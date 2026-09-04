package com.example.widget

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AppWidgetComposable(
    widgetHostManager: WidgetHostManager,
    appWidgetId: Int,
    providerInfo: AppWidgetProviderInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hostView = remember(appWidgetId) {
        widgetHostManager.createView(context, appWidgetId, providerInfo)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            hostView
        },
        update = {
            // Update logic if needed when re-composed
        }
    )
}
