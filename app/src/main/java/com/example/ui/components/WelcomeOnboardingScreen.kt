package com.example.ui.components

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LauncherApp
import com.example.model.LauncherThemeStyle
import com.example.util.DefaultAppsResolver

@Composable
fun WelcomeOnboardingScreen(
    isOpen: Boolean,
    installedApps: List<LauncherApp>,
    currentDockApps: List<LauncherApp>,
    isAiEnabled: Boolean,
    onToggleAi: (Boolean) -> Unit,
    onUpdateDockApps: (List<LauncherApp>) -> Unit,
    currentThemeStyle: LauncherThemeStyle,
    onSelectThemeStyle: (LauncherThemeStyle) -> Unit,
    onComplete: () -> Unit
) {
    if (!isOpen) return
    val context = LocalContext.current

    // Initialize default suggested apps for dock if currently empty
    LaunchedEffect(installedApps) {
        if (currentDockApps.isEmpty() && installedApps.isNotEmpty()) {
            val suggestedPkgs = DefaultAppsResolver.getDefaultAppsPackages(context)
            val suggested = installedApps.filter { it.packageName in suggestedPkgs }.take(5)
            if (suggested.isNotEmpty()) {
                onUpdateDockApps(suggested)
            } else {
                onUpdateDockApps(installedApps.take(4))
            }
        }
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF070B14), Color(0xFF0F172A), Color(0xFF1E1B4B))
                    )
                )
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF6366F1), Color(0xFFA855F7))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Front Launcher",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Bem-vindo ao Front Launcher",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configuração inicial simples e rápida",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 1. DEFINIR COMO PADRÃO
                OnboardingStepCard(
                    stepNumber = "1",
                    title = "Definir como Padrão",
                    description = "Para abrir o Front Launcher sempre que pressionar o botão Home ou deslizar para início.",
                    actionButton = {
                        OutlinedButton(
                            onClick = { launchDefaultHomeSettings(context) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir Seleção de Launcher", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. PERMISSÕES ESSENCIAIS
                OnboardingStepCard(
                    stepNumber = "2",
                    title = "Permissões Essenciais",
                    description = "Permitir leitura de apps instalados, plano de fundo, acessibilidade (gestos fluidos) e notificações.",
                    actionButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { launchUsageAccessSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Acesso ao Uso de Apps", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { launchAccessibilitySettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Acessibilidade (Gestos)", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { launchNotificationSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Ler Mensagens e Notificações", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { launchWallpaperPicker(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Carregar Plano de Fundo do Sistema", fontSize = 12.sp)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. CONFIGURAÇÃO OBRIGATÓRIA DO DOCK (ATÉ 5 APPS)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Apps do Dock (Obrigatório até 5)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "A IA sugeriu seus apps padrão. Selecione no mínimo 1 e no máximo 5 apps para a barra inferior fixa:",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selecionados: ${currentDockApps.size} de 5",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentDockApps.isNotEmpty() && currentDockApps.size <= 5)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFFEF4444)
                            )

                            OutlinedButton(
                                onClick = {
                                    val suggestedPkgs = DefaultAppsResolver.getDefaultAppsPackages(context)
                                    val suggested = installedApps.filter { it.packageName in suggestedPkgs }.take(5)
                                    if (suggested.isNotEmpty()) onUpdateDockApps(suggested)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                            ) {
                                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sugerir IA", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val availableApps = installedApps.filter { !it.isHidden }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableApps) { app ->
                                val isInDock = currentDockApps.any { it.packageName == app.packageName }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isInDock) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isInDock) 1.5.dp else 0.5.dp,
                                        color = if (isInDock) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.clickable {
                                        if (isInDock) {
                                            onUpdateDockApps(currentDockApps.filter { it.packageName != app.packageName })
                                        } else if (currentDockApps.size < 5) {
                                            onUpdateDockApps(currentDockApps + app)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isInDock) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = app.label,
                                            fontSize = 11.sp,
                                            color = if (isInDock) MaterialTheme.colorScheme.primary else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. ATIVAR/DESATIVAR IA NO LAUNCHER
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("4", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Inteligência Artificial no Launcher",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleAi(!isAiEnabled) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAiEnabled) "Ativar Inteligência Artificial (Recomendado)" else "IA Desativada (Modo Leve)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isAiEnabled)
                                        "Sugere rotinas diárias e categoriza seus apps automaticamente no dispositivo."
                                    else
                                        "O launcher funcionará de forma clássica sem processamento inteligente.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.65f),
                                    lineHeight = 15.sp
                                )
                            }

                            Switch(
                                checked = isAiEnabled,
                                onCheckedChange = onToggleAi,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. ESTILO VISUAL & TEMAS
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Estilo Visual Inicial",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LauncherThemeStyle.entries.forEach { style ->
                            val isSelected = style == currentThemeStyle
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectThemeStyle(style) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.Palette,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = style.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = style.description,
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.65f),
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val isDockValid = currentDockApps.isNotEmpty() && currentDockApps.size <= 5

                Button(
                    onClick = {
                        if (isDockValid) {
                            onComplete()
                        } else {
                            Toast.makeText(context, "Selecione entre 1 e 5 apps para o dock antes de continuar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDockValid,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(
                        text = "Começar a Usar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OnboardingStepCard(
    stepNumber: String,
    title: String,
    description: String,
    actionButton: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            actionButton()
        }
    }
}

private fun launchDefaultHomeSettings(context: Context) {
    val homeIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(homeIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Acesse Configurações para mudar o app de Início", Toast.LENGTH_LONG).show()
    }
}

private fun launchUsageAccessSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        Toast.makeText(context, "Acesse Configurações > Uso de Dados", Toast.LENGTH_SHORT).show()
    }
}

private fun launchAccessibilitySettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        Toast.makeText(context, "Acesse Configurações > Acessibilidade", Toast.LENGTH_SHORT).show()
    }
}

private fun launchNotificationSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        Toast.makeText(context, "Acesse Configurações > Acesso a Notificações", Toast.LENGTH_SHORT).show()
    }
}

private fun launchWallpaperPicker(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Plano de Fundo"))
    } catch (e: Exception) {
        Toast.makeText(context, "Plano de fundo do sistema ativo", Toast.LENGTH_SHORT).show()
    }
}
