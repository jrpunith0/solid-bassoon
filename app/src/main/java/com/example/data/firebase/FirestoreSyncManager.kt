package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.CachedGitHubRepoEntity
import com.example.data.CreatorRepository
import com.example.data.CreatorSettingsEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(
    private val context: Context,
    private val repository: CreatorRepository,
    private val scope: CoroutineScope
) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val firestore: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "Falling back to default Firestore: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    private var repoListenerRegistration: ListenerRegistration? = null
    private var settingsListenerRegistration: ListenerRegistration? = null

    fun getEffectiveUserId(settings: CreatorSettingsEntity): String {
        return auth.currentUser?.uid ?: settings.googleAccountId.ifBlank { "user_default_studio" }
    }

    fun startRealtimeSync(userId: String) {
        stopRealtimeSync()
        if (userId.isBlank()) return

        // 1. Listen in real-time to /users/{userId}/github_repos
        try {
            val reposRef = firestore.collection("users").document(userId).collection("github_repos")
            repoListenerRegistration = reposRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreSyncManager", "Real-time repos snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val repos = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: (doc.id.toLongOrNull() ?: 0L)
                            val name = doc.getString("name") ?: ""
                            if (name.isBlank()) return@mapNotNull null
                            CachedGitHubRepoEntity(
                                id = id,
                                name = name,
                                fullName = doc.getString("fullName") ?: name,
                                description = doc.getString("description"),
                                htmlUrl = doc.getString("htmlUrl") ?: "https://github.com",
                                language = doc.getString("language") ?: "Kotlin",
                                stargazersCount = doc.getLong("stargazersCount")?.toInt() ?: 0,
                                forksCount = doc.getLong("forksCount")?.toInt() ?: 0,
                                openIssuesCount = doc.getLong("openIssuesCount")?.toInt() ?: 0,
                                defaultBranch = doc.getString("defaultBranch") ?: "main",
                                updatedAt = doc.getString("updatedAt"),
                                isPrivate = doc.getBoolean("isPrivate") ?: false,
                                licenseName = doc.getString("licenseName"),
                                licenseSpdxId = doc.getString("licenseSpdxId"),
                                size = doc.getLong("size")?.toInt() ?: 0,
                                cachedAt = doc.getLong("cachedAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (repos.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            repository.cacheGitHubRepos(repos)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "Failed to attach repos snapshot listener: ${e.message}")
        }

        // 2. Listen in real-time to /users/{userId} for settings
        try {
            val userRef = firestore.collection("users").document(userId)
            settingsListenerRegistration = userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreSyncManager", "Real-time user settings snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val channelName = snapshot.getString("channelName")
                    val lang = snapshot.getString("defaultLanguage")
                    val theme = snapshot.getString("theme")
                    if (channelName != null) {
                        scope.launch(Dispatchers.IO) {
                            val current = repository.getSettingsOnce() ?: CreatorSettingsEntity()
                            val updated = current.copy(
                                channelName = channelName,
                                defaultLanguage = lang ?: current.defaultLanguage,
                                theme = theme ?: current.theme
                            )
                            repository.saveSettings(updated)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "Failed to attach user snapshot listener: ${e.message}")
        }
    }

    fun pushReposToFirestore(userId: String, repos: List<CachedGitHubRepoEntity>) {
        if (userId.isBlank() || repos.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            try {
                val batch = firestore.batch()
                val reposRef = firestore.collection("users").document(userId).collection("github_repos")
                for (repo in repos) {
                    val docRef = reposRef.document(repo.id.toString())
                    val data = mapOf(
                        "id" to repo.id,
                        "name" to repo.name,
                        "fullName" to repo.fullName,
                        "description" to (repo.description ?: ""),
                        "htmlUrl" to repo.htmlUrl,
                        "language" to (repo.language ?: "Kotlin"),
                        "stargazersCount" to repo.stargazersCount,
                        "forksCount" to repo.forksCount,
                        "openIssuesCount" to repo.openIssuesCount,
                        "defaultBranch" to repo.defaultBranch,
                        "updatedAt" to (repo.updatedAt ?: ""),
                        "isPrivate" to repo.isPrivate,
                        "licenseName" to (repo.licenseName ?: ""),
                        "licenseSpdxId" to (repo.licenseSpdxId ?: ""),
                        "size" to repo.size,
                        "cachedAt" to repo.cachedAt
                    )
                    batch.set(docRef, data, SetOptions.merge())
                }
                batch.commit().await()
                Log.d("FirestoreSyncManager", "Pushed ${repos.size} repos to Firestore successfully")
            } catch (e: Exception) {
                Log.w("FirestoreSyncManager", "Failed to push repos to Firestore: ${e.message}")
            }
        }
    }

    fun pushSettingsToFirestore(userId: String, settings: CreatorSettingsEntity) {
        if (userId.isBlank()) return
        scope.launch(Dispatchers.IO) {
            try {
                val userRef = firestore.collection("users").document(userId)
                val data = mapOf(
                    "userId" to userId,
                    "channelName" to settings.channelName,
                    "email" to settings.youTubeEmail,
                    "displayName" to settings.googleAccountName,
                    "defaultLanguage" to settings.defaultLanguage,
                    "theme" to settings.theme,
                    "updatedAt" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                )
                userRef.set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w("FirestoreSyncManager", "Failed to push settings to Firestore: ${e.message}")
            }
        }
    }

    fun stopRealtimeSync() {
        repoListenerRegistration?.remove()
        repoListenerRegistration = null
        settingsListenerRegistration?.remove()
        settingsListenerRegistration = null
    }
}
