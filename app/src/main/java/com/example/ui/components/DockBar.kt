package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IconShape
import com.example.model.LauncherApp
import com.example.model.LauncherThemeStyle

@Composable
fun DockBar(
    dockApps: List<LauncherApp>,
    iconShape: IconShape,
    iconSizeDp: Int,
    iconThemed: Boolean,
    themeStyle: LauncherThemeStyle,
    dockBlurColor: Color,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
            .testTag("launcher_dock_bar")
    ) {
        // Subtle swipe up handle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onOpenDrawer() }
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Deslize para cima para abrir apps",
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Deslize para ver todos",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dock container styled per launcher theme
        val dockShape = when (themeStyle) {
            LauncherThemeStyle.MATERIAL_YOU -> RoundedCornerShape(32.dp)
            LauncherThemeStyle.ONE_UI -> RoundedCornerShape(26.dp)
            LauncherThemeStyle.HYPER_OS -> RoundedCornerShape(28.dp)
        }

        val dockModifier = when (themeStyle) {
            LauncherThemeStyle.HYPER_OS -> Modifier
                .fillMaxWidth(0.92f)
                .clip(dockShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), dockShape)

            LauncherThemeStyle.ONE_UI -> Modifier
                .fillMaxWidth(0.92f)
                .clip(dockShape)
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), dockShape)

            LauncherThemeStyle.MATERIAL_YOU -> Modifier
                .fillMaxWidth(0.92f)
                .clip(dockShape)
                .background(dockBlurColor)
                .border(1.dp, Color.White.copy(alpha = 0.15f), dockShape)
        }

        Box(
            modifier = dockModifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (app in dockApps) {
                    androidx.compose.runtime.key(app.packageName) {
                        AppIconComposable(
                            app = app,
                            iconShape = iconShape,
                            iconSizeDp = (iconSizeDp * 0.95).toInt(),
                            iconThemed = iconThemed,
                            showLabel = false,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            }
        }
    }
}
