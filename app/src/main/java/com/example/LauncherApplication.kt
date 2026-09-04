package com.example

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings

class LauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        // Enable Firestore offline persistence with unlimited cache
        try {
            val firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings {
                    setSizeBytes(Long.MAX_VALUE)
                })
            }
            Firebase.firestore.firestoreSettings = firestoreSettings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize Analytics & Anonymous Auth
        com.example.util.LauncherAnalytics.init(this)
        com.example.data.FirebaseSyncManager.ensureAuthenticated()
    }
}
