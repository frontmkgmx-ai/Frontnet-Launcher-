package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class WallpaperTheme(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val surfaceColor: Color,
    val backgroundBrush: Brush,
    val previewColors: List<Color>,
    val dockBlurColor: Color,
    val isOledDark: Boolean = false
)

object WallpaperThemeEngine {

    val wallpaperList = listOf(
        WallpaperTheme(
            id = "monet_aurora",
            name = "Monet Aurora",
            description = "Paleta dinâmica Material You com tons cósmicos de ciano e violeta",
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFFA78BFA),
            tertiaryColor = Color(0xFFF472B6),
            surfaceColor = Color(0xFF0F172A),
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0B0F19),
                    Color(0xFF131C31),
                    Color(0xFF0F172A),
                    Color(0xFF050810)
                )
            ),
            previewColors = listOf(Color(0xFF38BDF8), Color(0xFFA78BFA), Color(0xFF0F172A)),
            dockBlurColor = Color(0x331E293B)
        ),
        WallpaperTheme(
            id = "hyper_glass",
            name = "HyperOS Cobalt Glass",
            description = "Estilo futurista HyperOS com efeito translúcido e reflexos gelados",
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF2979FF),
            tertiaryColor = Color(0xFF7C4DFF),
            surfaceColor = Color(0xFF0A101D),
            backgroundBrush = Brush.radialGradient(
                listOf(
                    Color(0xFF1A2639),
                    Color(0xFF0D1524),
                    Color(0xFF060B12)
                )
            ),
            previewColors = listOf(Color(0xFF00E5FF), Color(0xFF2979FF), Color(0xFF0A101D)),
            dockBlurColor = Color(0x4D00E5FF)
        ),
        WallpaperTheme(
            id = "oneui_pure",
            name = "One UI Violet Sunset",
            description = "Gradiente suave característico do One UI com violeta e orquídea profundo",
            primaryColor = Color(0xFFC084FC),
            secondaryColor = Color(0xFFF472B6),
            tertiaryColor = Color(0xFF60A5FA),
            surfaceColor = Color(0xFF1A1024),
            backgroundBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF221330),
                    Color(0xFF1E112A),
                    Color(0xFF120A1A)
                )
            ),
            previewColors = listOf(Color(0xFFC084FC), Color(0xFFF472B6), Color(0xFF1A1024)),
            dockBlurColor = Color(0x332E1A40)
        ),
        WallpaperTheme(
            id = "emerald_nature",
            name = "Monet Botânico",
            description = "Inspirado na natureza com tons de esmeralda, sálvia e âmbar suave",
            primaryColor = Color(0xFF34D399),
            secondaryColor = Color(0xFF6EE7B7),
            tertiaryColor = Color(0xFFFBBF24),
            surfaceColor = Color(0xFF062016),
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF041911),
                    Color(0xFF08261A),
                    Color(0xFF051710)
                )
            ),
            previewColors = listOf(Color(0xFF34D399), Color(0xFF6EE7B7), Color(0xFF062016)),
            dockBlurColor = Color(0x330B3323)
        ),
        WallpaperTheme(
            id = "solar_sunset",
            name = "Material Terracota",
            description = "Tons quentes aconchegantes com pêssego, âmbar e areia dourada",
            primaryColor = Color(0xFFFB923C),
            secondaryColor = Color(0xFFF59E0B),
            tertiaryColor = Color(0xFFEF4444),
            surfaceColor = Color(0xFF20130C),
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF23120A),
                    Color(0xFF2A190F),
                    Color(0xFF160A05)
                )
            ),
            previewColors = listOf(Color(0xFFFB923C), Color(0xFFF59E0B), Color(0xFF20130C)),
            dockBlurColor = Color(0x333D2012)
        ),
        WallpaperTheme(
            id = "cyber_dark",
            name = "Hyper Obsidian OLED",
            description = "Preto absoluto com alto contraste, ideal para telas AMOLED",
            primaryColor = Color(0xFFA3E635),
            secondaryColor = Color(0xFF38BDF8),
            tertiaryColor = Color(0xFFE2E8F0),
            surfaceColor = Color(0xFF000000),
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF000000),
                    Color(0xFF050505),
                    Color(0xFF000000)
                )
            ),
            previewColors = listOf(Color(0xFFA3E635), Color(0xFF38BDF8), Color(0xFF000000)),
            dockBlurColor = Color(0x33222222),
            isOledDark = true
        )
    )

    fun getThemeById(id: String): WallpaperTheme {
        return wallpaperList.firstOrNull { it.id == id } ?: wallpaperList.first()
    }

    fun buildColorScheme(theme: WallpaperTheme, isDark: Boolean = true): ColorScheme {
        return if (isDark) {
            darkColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color.Black,
                primaryContainer = theme.primaryColor.copy(alpha = 0.2f),
                onPrimaryContainer = theme.primaryColor,
                secondary = theme.secondaryColor,
                onSecondary = Color.Black,
                secondaryContainer = theme.secondaryColor.copy(alpha = 0.2f),
                onSecondaryContainer = theme.secondaryColor,
                tertiary = theme.tertiaryColor,
                background = theme.surfaceColor,
                onBackground = Color(0xFFF1F5F9),
                surface = theme.surfaceColor,
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = theme.surfaceColor.copy(alpha = 0.85f),
                onSurfaceVariant = Color(0xFFCBD5E1)
            )
        } else {
            lightColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color.White,
                secondary = theme.secondaryColor,
                tertiary = theme.tertiaryColor,
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF)
            )
        }
    }
}
