package com.example.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

object LauncherAnalytics {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    fun logAppLaunch(packageName: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, packageName)
                putString(FirebaseAnalytics.Param.ITEM_NAME, packageName)
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, "application")
            }
            firebaseAnalytics?.logEvent("app_opened", bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logPageChanged(pageIndex: Int) {
        try {
            val bundle = Bundle().apply {
                putLong("page_index", pageIndex.toLong())
            }
            firebaseAnalytics?.logEvent("home_page_changed", bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTrace(traceName: String): Trace? {
        return try {
            val trace = FirebasePerformance.getInstance().newTrace(traceName)
            trace.start()
            trace
        } catch (e: Exception) {
            null
        }
    }

    fun stopTrace(trace: Trace?) {
        try {
            trace?.stop()
        } catch (e: Exception) {
            // Ignored
        }
    }
}
