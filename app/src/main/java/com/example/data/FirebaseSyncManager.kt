package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseSyncManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun ensureAuthenticated(onReady: (String?) -> Unit = {}) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onReady(currentUser.uid)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = auth.signInAnonymously().await()
                    onReady(result.user?.uid)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onReady(null)
                }
            }
        }
    }

    fun syncUserData(
        settingsMap: Map<String, Any>,
        desktopLayout: List<Any>,
        dockApps: List<String>,
        installedPackages: List<String>
    ) {
        val uid = auth.currentUser?.uid ?: return
        val userDocRef = db.collection("users").document(uid)

        val data = mapOf(
            "settings" to settingsMap,
            "desktop_layout" to desktopLayout,
            "dock_apps" to dockApps,
            "installed_packages" to installedPackages,
            "updated_at" to System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                userDocRef.set(data).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadUserDataOfflineFirst(
        onDataLoaded: (Map<String, Any>?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onDataLoaded(null)
            return
        }
        val userDocRef = db.collection("users").document(uid)

        // Read cache first (offline-first)
        userDocRef.get(Source.CACHE).addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                onDataLoaded(document.data)
            } else {
                userDocRef.get(Source.DEFAULT).addOnSuccessListener { serverDoc ->
                    onDataLoaded(serverDoc?.data)
                }.addOnFailureListener {
                    onDataLoaded(null)
                }
            }
        }.addOnFailureListener {
            userDocRef.get(Source.DEFAULT).addOnSuccessListener { serverDoc ->
                onDataLoaded(serverDoc?.data)
            }.addOnFailureListener {
                onDataLoaded(null)
            }
        }

        // Realtime listener for remote changes
        userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                onDataLoaded(snapshot.data)
            }
        }
    }

    fun deleteUserData(onComplete: (Boolean) -> Unit = {}) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        if (uid == null) {
            onComplete(true)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("users").document(uid).delete().await()
                currentUser.delete().await()
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}
