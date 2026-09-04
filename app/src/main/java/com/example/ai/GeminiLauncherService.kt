package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.AiDailyBriefing
import com.example.model.AiShortcutSuggestion
import com.example.model.AppCategory
import com.example.model.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

object GeminiLauncherService {

    private const val TAG = "GeminiLauncher"
    private const val MODEL = "gemini-3.5-flash"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getDailyRoutineSuggestions(
        installedApps: List<LauncherApp>,
        topUsedApps: List<LauncherApp>
    ): AiDailyBriefing = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Segunda-feira"
            Calendar.TUESDAY -> "Terça-feira"
            Calendar.WEDNESDAY -> "Quarta-feira"
            Calendar.THURSDAY -> "Quinta-feira"
            Calendar.FRIDAY -> "Sexta-feira"
            Calendar.SATURDAY -> "Sábado"
            else -> "Domingo"
        }

        val timePeriod = when (hour) {
            in 5..11 -> "Manhã (Início de rotina diária)"
            in 12..13 -> "Meio-dia (Pausa para almoço)"
            in 14..18 -> "Tarde (Foco em produtividade e trabalho)"
            in 19..22 -> "Noite (Lazer, relaxamento e redes sociais)"
            else -> "Madrugada (Descanso e silêncio)"
        }

        // Try calling Gemini API if key is present
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (!apiKey.isNullOrBlank() && !apiKey.contains("MY_GEMINI_API_KEY")) {
            try {
                val candidateAppNames = installedApps.take(20).joinToString(", ") { "${it.label} (${it.packageName})" }
                val prompt = """
                    Você é a Inteligência Artificial contextual de um Android Home Launcher.
                    Contexto atual do usuário:
                    - Dia: $dayOfWeek
                    - Horário: $hour:00 ($timePeriod)
                    - Apps disponíveis no dispositivo: $candidateAppNames
                    
                    Gere uma sugestão de rotina diária em JSON com exatamente esta estrutura:
                    {
                      "greeting": "Frase curta e calorosa em Português para este momento",
                      "contextInsight": "Insight prático de 1 frase recomendando foco ou atividade atual",
                      "quickActionTitle": "Ação rápida recomendada (ex: Iniciar foco, Checar trânsito, Relaxar com música)",
                      "quickActionPrompt": "Comando de voz ou ação imediata correspondente",
                      "recommendedAppPackages": ["pacote1", "pacote2", "pacote3", "pacote4"],
                      "reasons": ["Motivo 1", "Motivo 2", "Motivo 3", "Motivo 4"]
                    }
                    Selecione preferencialmente os pacotes da lista fornecida que façam sentido para o horário ($hour:00).
                    Retorne apenas JSON válido.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    }
                    put("contents", contentsArray)
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                    })
                }

                val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
                val httpRequest = Request.Builder()
                    .url("$API_URL?key=$apiKey")
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string()
                    if (!responseStr.isNullOrBlank()) {
                        val parsed = parseGeminiResponse(responseStr, installedApps, timePeriod)
                        if (parsed != null) {
                            return@withContext parsed.copy(isLiveGeminiResponse = true)
                        }
                    }
                } else {
                    Log.w(TAG, "Gemini API error code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed calling Gemini API, falling back to local AI engine", e)
            }
        }

        // Fallback to high-fidelity contextual heuristic engine
        generateLocalSmartBriefing(hour, dayOfWeek, timePeriod, installedApps, topUsedApps)
    }

    private fun parseGeminiResponse(
        responseBody: String,
        installedApps: List<LauncherApp>,
        timePeriod: String
    ): AiDailyBriefing? {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val textPart = parts.optJSONObject(0)?.optString("text") ?: return null

            val jsonObject = JSONObject(textPart.trim().removePrefix("```json").removePrefix("```").removeSuffix("```"))
            val greeting = jsonObject.optString("greeting", "Olá! Bom momento do dia")
            val contextInsight = jsonObject.optString("contextInsight", "Seus atalhos inteligentes estão prontos para você.")
            val quickActionTitle = jsonObject.optString("quickActionTitle", "Ação rápida sugerida")
            val quickActionPrompt = jsonObject.optString("quickActionPrompt", "Toque para iniciar sua rotina")

            val packagesArray = jsonObject.optJSONArray("recommendedAppPackages")
            val reasonsArray = jsonObject.optJSONArray("reasons")

            val recommendedShortcuts = mutableListOf<AiShortcutSuggestion>()
            if (packagesArray != null) {
                for (i in 0 until packagesArray.length()) {
                    val pkg = packagesArray.optString(i)
                    val matchedApp = installedApps.firstOrNull { it.packageName.contains(pkg, ignoreCase = true) }
                    val reason = reasonsArray?.optString(i) ?: "Recomendado para $timePeriod"

                    if (matchedApp != null) {
                        recommendedShortcuts.add(
                            AiShortcutSuggestion(
                                title = matchedApp.label,
                                subtitle = reason,
                                packageName = matchedApp.packageName,
                                iconVector = matchedApp.iconVector,
                                reason = reason,
                                category = matchedApp.category
                            )
                        )
                    }
                }
            }

            // Fill up to 4 if needed
            if (recommendedShortcuts.isEmpty()) {
                return null
            }

            AiDailyBriefing(
                greeting = greeting,
                contextInsight = contextInsight,
                timeSlotLabel = timePeriod,
                recommendedApps = recommendedShortcuts.take(4),
                quickActionTitle = quickActionTitle,
                quickActionPrompt = quickActionPrompt,
                isLiveGeminiResponse = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
            null
        }
    }

    private fun generateLocalSmartBriefing(
        hour: Int,
        dayOfWeek: String,
        timePeriod: String,
        installedApps: List<LauncherApp>,
        topUsedApps: List<LauncherApp>
    ): AiDailyBriefing {
        val (greeting, insight, quickAction, actionPrompt, targetCategories) = when (hour) {
            in 5..11 -> RoutineProfile(
                greeting = "🌅 Bom dia!",
                insight = "Comece o dia com foco: confira seus compromissos, previsão e mensagens.",
                quickAction = "Abrir Planejamento Matinal",
                actionPrompt = "Verifique sua agenda e tarefas prioritárias de hoje.",
                categories = listOf(AppCategory.PRODUCTIVITY, AppCategory.COMMUNICATION, AppCategory.TOOLS, AppCategory.TRAVEL)
            )
            in 12..13 -> RoutineProfile(
                greeting = "☀️ Hora do Almoço",
                insight = "Momento de pausa: que tal conferir mensagens de amigos ou notícias?",
                quickAction = "Pausa Rápida",
                actionPrompt = "Desconecte-se um pouco antes de retornar às tarefas.",
                categories = listOf(AppCategory.FINANCE, AppCategory.COMMUNICATION, AppCategory.MEDIA, AppCategory.OTHER)
            )
            in 14..18 -> RoutineProfile(
                greeting = "⚡ Tarde Produtiva",
                insight = "Mantenha o ritmo: ferramentas de trabalho e comunicação no topo.",
                quickAction = "Modo Foco Ativo",
                actionPrompt = "Acessar documentos, e-mails e anotações rápidas.",
                categories = listOf(AppCategory.PRODUCTIVITY, AppCategory.TOOLS, AppCategory.COMMUNICATION, AppCategory.FINANCE)
            )
            in 19..22 -> RoutineProfile(
                greeting = "🌙 Boa noite!",
                insight = "Hora de descontrair: ouça suas músicas favoritas, assista vídeos ou jogue.",
                quickAction = "Relaxamento Noturno",
                actionPrompt = "Explore playlists calmas e novidades dos seus criadores.",
                categories = listOf(AppCategory.MEDIA, AppCategory.GAMES, AppCategory.COMMUNICATION, AppCategory.OTHER)
            )
            else -> RoutineProfile(
                greeting = "✨ Madrugada Tranquila",
                insight = "Alarme matinal configurado? Modo não perturbe recomendado.",
                quickAction = "Configurar Alarme",
                actionPrompt = "Verifique o despertador e ative o protetor ocular.",
                categories = listOf(AppCategory.TOOLS, AppCategory.MEDIA, AppCategory.COMMUNICATION, AppCategory.OTHER)
            )
        }

        // Pick top apps prioritizing targetCategories and top usage
        val selectedApps = mutableListOf<AiShortcutSuggestion>()

        for (category in targetCategories) {
            val app = installedApps.firstOrNull { it.category == category && selectedApps.none { s -> s.packageName == it.packageName } }
                ?: topUsedApps.firstOrNull { it.category == category && selectedApps.none { s -> s.packageName == it.packageName } }
            if (app != null) {
                selectedApps.add(
                    AiShortcutSuggestion(
                        title = app.label,
                        subtitle = "Sugerido para $timePeriod",
                        packageName = app.packageName,
                        iconVector = app.iconVector,
                        reason = "Recomendado com base na rotina de $dayOfWeek",
                        category = app.category
                    )
                )
            }
        }

        // Fill remaining from general installed apps
        for (app in installedApps) {
            if (selectedApps.size >= 4) break
            if (selectedApps.none { it.packageName == app.packageName }) {
                selectedApps.add(
                    AiShortcutSuggestion(
                        title = app.label,
                        subtitle = "Atalho rápido",
                        packageName = app.packageName,
                        iconVector = app.iconVector,
                        reason = "Acesso rápido inteligente",
                        category = app.category
                    )
                )
            }
        }

        return AiDailyBriefing(
            greeting = "$greeting $dayOfWeek",
            contextInsight = insight,
            timeSlotLabel = timePeriod,
            recommendedApps = selectedApps.take(4),
            quickActionTitle = quickAction,
            quickActionPrompt = actionPrompt,
            isLiveGeminiResponse = false
        )
    }

    private data class RoutineProfile(
        val greeting: String,
        val insight: String,
        val quickAction: String,
        val actionPrompt: String,
        val categories: List<AppCategory>
    )
}
