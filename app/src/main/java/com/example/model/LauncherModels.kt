package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    COMMUNICATION("Comunicação & Social", "Mensagens, redes e contatos", Icons.AutoMirrored.Rounded.Chat, Color(0xFF38BDF8)),
    PRODUCTIVITY("Produtividade", "Trabalho, estudos e organização", Icons.Rounded.Work, Color(0xFF818CF8)),
    TOOLS("Ferramentas & Sistema", "Utilitários, configurações e sistema", Icons.Rounded.Build, Color(0xFFFBBF24)),
    MEDIA("Mídia & Streaming", "Músicas, vídeos e fotos", Icons.Rounded.PlayCircle, Color(0xFFF43F5E)),
    GAMES("Jogos", "Entretenimento e diversão", Icons.Rounded.SportsEsports, Color(0xFFA855F7)),
    FINANCE("Finanças & Compras", "Bancos, pagamentos e compras", Icons.Rounded.AccountBalance, Color(0xFF34D399)),
    TRAVEL("Navegação & Viagens", "Mapas, transporte e viagens", Icons.Rounded.Explore, Color(0xFFFB923C)),
    OTHER("Outros Aplicativos", "Demais aplicativos instalados", Icons.Rounded.Apps, Color(0xFF94A3B8));

    companion object {
        fun fromName(name: String): AppCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class IconShape(val label: String, val cornerPercent: Int) {
    SQUIRCLE("One UI Squircle", 32),
    ROUNDED_SQUARE("HyperOS Suave", 24),
    CIRCLE("Material You Círculo", 50),
    TEARDROP("Gota Dinâmica", 16)
}

enum class HomeScreenStyle(val displayName: String) {
    CLASSIC_GRID("Grade Clássica"),
    MINIMALIST_TEXT("Texto Minimalista"),
    DOCK_ONLY("Apenas Dock expansível")
}

enum class AppDrawerStyle(val displayName: String) {
    VERTICAL_GRID("Grade Vertical contínua"),
    ALPHABETICAL_LIST("Lista Alfabética (A-Z)"),
    HORIZONTAL_PAGED("Páginas Horizontais"),
    CATEGORY_TABS("Abas por Categoria (IA)")
}

enum class LauncherThemeStyle(val displayName: String, val description: String) {
    MATERIAL_YOU("Material You Monet", "Design dinâmico do Android com paleta harmônica e cantos pílula"),
    ONE_UI("Samsung One UI", "Ergonomia focada na metade inferior, cards fáceis de alcançar e squircle"),
    HYPER_OS("Xiaomi HyperOS", "Estilo futurista com vidro fosco (glassmorphism), super dock e tipografia nítida")
}

enum class GestureAction(val displayName: String) {
    APP_DRAWER("Abrir Gaveta de Apps"),
    SEARCH("Busca Rápida e IA"),
    AI_ROUTINE("Resumo e Atalhos da IA"),
    CUSTOMIZE("Personalizar Launcher e Papel de Parede"),
    SETTINGS("Configurações do Front Launcher"),
    NOTIFICATIONS("Expandir Barra de Status")
}

data class LauncherApp(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val category: AppCategory = AppCategory.OTHER,
    val iconDrawable: Drawable? = null,
    val iconVector: ImageVector? = null,
    val iconTint: Color? = null,
    val launchCount: Int = 0,
    val lastLaunchedTimestamp: Long = 0L,
    val isPinnedToDock: Boolean = false,
    val isFavorite: Boolean = false,
    val isSystemDefault: Boolean = false,
    val isLauncherSettingsShortcut: Boolean = false
)

data class AiShortcutSuggestion(
    val title: String,
    val subtitle: String,
    val packageName: String,
    val iconVector: ImageVector? = null,
    val reason: String,
    val category: AppCategory
)

data class AiDailyBriefing(
    val greeting: String,
    val contextInsight: String,
    val timeSlotLabel: String,
    val recommendedApps: List<AiShortcutSuggestion>,
    val quickActionTitle: String,
    val quickActionPrompt: String,
    val isLiveAiResponse: Boolean = false
)
