package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.LauncherThemeStyle
import com.example.ui.theme.WallpaperTheme
import com.example.ui.theme.WallpaperThemeEngine

import com.example.model.AppDrawerStyle
import com.example.model.HomeScreenStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizeLauncherSheet(
    isOpen: Boolean,
    themeStyle: LauncherThemeStyle,
    homeScreenStyle: HomeScreenStyle = HomeScreenStyle.CLASSIC_GRID,
    appDrawerStyle: AppDrawerStyle = AppDrawerStyle.CATEGORY_TABS,
    iconShape: IconShape,
    iconThemed: Boolean,
    iconSizeDp: Int,
    showLabels: Boolean,
    currentWallpaper: WallpaperTheme,
    gestureSwipeDown: GestureAction,
    gestureDoubleTap: GestureAction,
    highRefreshRateEnabled: Boolean,
    onThemeStyleChange: (LauncherThemeStyle) -> Unit,
    onHomeScreenStyleChange: ((HomeScreenStyle) -> Unit)? = null,
    onAppDrawerStyleChange: ((AppDrawerStyle) -> Unit)? = null,
    onIconShapeChange: (IconShape) -> Unit,
    onIconThemedChange: (Boolean) -> Unit,
    onIconSizeChange: (Int) -> Unit,
    onShowLabelsChange: (Boolean) -> Unit,
    onWallpaperChange: (WallpaperTheme) -> Unit,
    onGestureSwipeDownChange: (GestureAction) -> Unit,
    onGestureDoubleTapChange: (GestureAction) -> Unit,
    onHighRefreshRateChange: (Boolean) -> Unit,
    onOpenWelcomeOnboarding: () -> Unit,
    onAddWidgetClick: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("customize_launcher_sheet")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Personalizar Launcher",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ícones, Gestos, Temas e Papel de Parede",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section 1: Launcher Experience & Theme Style (Material You, One UI, HyperOS)
                item {
                    SettingsCard(
                        title = "Estilo de Interface",
                        subtitle = "Compatível com sistemas modernos",
                        icon = Icons.Rounded.FormatPaint
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LauncherThemeStyle.entries.forEach { style ->
                                val isSelected = style == themeStyle
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onThemeStyleChange(style) }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = style.displayName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                            )
                                            Text(
                                                text = style.description,
                                                fontSize = 12.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SettingsCard(
                        title = "Widgets Android",
                        subtitle = "Adicione widgets nativos à tela inicial",
                        icon = Icons.Rounded.SmartButton
                    ) {
                        OutlinedButton(
                            onClick = onAddWidgetClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adicionar Novo Widget")
                        }
                    }
                }

                // Section 2: Dynamic Wallpapers & Monet Palette Extraction
                item {
                    SettingsCard(
                        title = "Papel de Parede & Tema Dinâmico",
                        subtitle = "Cores extraídas e paleta Monet aplicada em todo o launcher",
                        icon = Icons.Rounded.Wallpaper
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(WallpaperThemeEngine.wallpaperList) { wallpaper ->
                                val isSelected = wallpaper.id == currentWallpaper.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { onWallpaperChange(wallpaper) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 100.dp, height = 140.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(wallpaper.backgroundBrush)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(18.dp)
                                            ),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        // Preview palette pills at bottom of wallpaper card
                                        Row(
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            wallpaper.previewColors.forEach { c ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(c)
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = wallpaper.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Customizable Icons (Shape, Size, Themed, Labels)
                item {
                    SettingsCard(
                        title = "Customização de Ícones",
                        subtitle = "Formatos One UI, HyperOS, Material You e ícones temáticos",
                        icon = Icons.Rounded.SmartButton
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Icon Shapes
                            Text(
                                text = "Formato dos Ícones",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconShape.entries.forEach { shape ->
                                    val isSelected = shape == iconShape
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onIconShapeChange(shape) }
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                        ) {
                                            // Shape preview glyph
                                            val previewShape = when (shape) {
                                                IconShape.CIRCLE -> CircleShape
                                                IconShape.SQUIRCLE -> RoundedCornerShape(30)
                                                IconShape.ROUNDED_SQUARE -> RoundedCornerShape(22)
                                                IconShape.TEARDROP -> RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 50, bottomEndPercent = 12)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(previewShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f))
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = shape.label.split(" ").first(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Themed Icons Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ícones Temáticos Material You",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Aplica tom monocromático harmônico com o papel de parede",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Switch(
                                    checked = iconThemed,
                                    onCheckedChange = onIconThemedChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }

                            // Show Labels Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Exibir Nomes dos Apps",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Oculte para um visual minimalista e limpo",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Switch(
                                    checked = showLabels,
                                    onCheckedChange = onShowLabelsChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }

                            // Icon Size Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tamanho dos Ícones",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "${iconSizeDp}dp",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = iconSizeDp.toFloat(),
                                    onValueChange = { onIconSizeChange(it.toInt()) },
                                    valueRange = 44f..68f,
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // Section 4: Smart Navigation Gestures
                item {
                    SettingsCard(
                        title = "Gestos Inteligentes de Navegação",
                        subtitle = "Configure as ações de deslize e toques na tela inicial",
                        icon = Icons.Rounded.Gesture
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Fixed gesture info
                            GestureInfoRow(
                                gesture = "Deslizar para Cima",
                                action = "Abrir Gaveta de Aplicativos (Organização IA)"
                            )
                            GestureInfoRow(
                                gesture = "Pinçar na Tela (Pinch)",
                                action = "Abrir Personalização do Launcher"
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Configurable: Deslizar para Baixo
                            Text(
                                text = "Ação: Deslizar para Baixo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(GestureAction.SEARCH, GestureAction.AI_ROUTINE, GestureAction.APP_DRAWER, GestureAction.NOTIFICATIONS).forEach { action ->
                                    val isSelected = action == gestureSwipeDown
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .clickable { onGestureSwipeDownChange(action) }
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Text(
                                            text = action.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Configurable: Toque Duplo
                            Text(
                                text = "Ação: Toque Duplo no Espaço Vazio",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(GestureAction.AI_ROUTINE, GestureAction.CUSTOMIZE, GestureAction.SEARCH).forEach { action ->
                                    val isSelected = action == gestureDoubleTap
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .clickable { onGestureDoubleTapChange(action) }
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Text(
                                            text = action.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 6: Performance & Refresh Rate (Até 120 FPS)
                item {
                    SettingsCard(
                        title = "Taxa de Atualização & Desempenho",
                        subtitle = "Máxima fluidez até 120 FPS",
                        icon = Icons.Rounded.Speed
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Modo Ultra Fluido (120 Hz / 120 FPS)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Requisita o modo de exibição de taxa mais alta (90Hz / 120Hz) suportado pelo hardware do celular",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.65f),
                                        lineHeight = 15.sp
                                    )
                                }
                                Switch(
                                    checked = highRefreshRateEnabled,
                                    onCheckedChange = onHighRefreshRateChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF00E5FF),
                                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Section 7: Default Launcher & Initial Setup
                item {
                    SettingsCard(
                        title = "Configuração Inicial do Launcher",
                        subtitle = "Definir padrão e permissões",
                        icon = Icons.Rounded.Tune
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Reveja a tela de boas-vindas com o assistente de configuração de launcher padrão e otimizações.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                lineHeight = 16.sp
                            )

                            OutlinedButton(
                                onClick = onOpenWelcomeOnboarding,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir Assistente de Boas-Vindas", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun GestureInfoRow(
    gesture: String,
    action: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = gesture,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = action,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
