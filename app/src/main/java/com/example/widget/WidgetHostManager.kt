package com.example.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class WidgetHostManager(context: Context) {
    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost: AppWidgetHost = AppWidgetHost(context, HOST_ID)

    fun startListening() {
        try {
            appWidgetHost.startListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            appWidgetHost.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun allocateAppWidgetId(): Int {
        return try {
            appWidgetHost.allocateAppWidgetId()
        } catch (e: Exception) {
            -1
        }
    }

    fun deleteAppWidgetId(appWidgetId: Int) {
        try {
            appWidgetHost.deleteAppWidgetId(appWidgetId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createView(context: Context, appWidgetId: Int, appWidgetInfo: AppWidgetProviderInfo): AppWidgetHostView? {
        return try {
            appWidgetHost.createView(context, appWidgetId, appWidgetInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        const val HOST_ID = 1024
    }
}
