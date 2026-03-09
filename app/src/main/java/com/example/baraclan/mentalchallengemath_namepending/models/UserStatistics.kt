package com.example.baraclan.mentalchallengemath_namepending.models

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────
// XP awarded per game outcome
// ─────────────────────────────────────────────────────────────
object XpValues {
    const val WIN = 100
    const val LOSS = 20
    const val TIE = 50
    const val GAME_PLAYED = 10
}

// ─────────────────────────────────────────────────────────────
// Level system — 10 levels with increasing XP thresholds
// ─────────────────────────────────────────────────────────────
object LevelSystem {
    private val thresholds = listOf(0, 200, 500, 900, 1400, 2000, 2700, 3500, 4400, 5400)
    val maxLevel = thresholds.size

    fun levelFromXp(xp: Int): Int {
        var level = 1
        for (i in thresholds.indices) {
            if (xp >= thresholds[i]) level = i + 1 else break
        }
        return level.coerceAtMost(maxLevel)
    }

    fun xpForLevel(level: Int): Int = thresholds.getOrElse(level - 1) { 0 }

    fun xpForNextLevel(xp: Int): Int {
        val level = levelFromXp(xp)
        return thresholds.getOrElse(level) { Int.MAX_VALUE }
    }

    // Progress within current level: 0.0 → 1.0
    fun levelProgress(xp: Int): Float {
        val level = levelFromXp(xp)
        if (level >= maxLevel) return 1f
        val start = thresholds.getOrElse(level - 1) { 0 }
        val end = thresholds.getOrElse(level) { 1 }
        return ((xp - start).toFloat() / (end - start).toFloat()).coerceIn(0f, 1f)
    }

    // XP within current level (shown as "X / Y XP")
    fun xpInCurrentLevel(xp: Int): Int {
        val level = levelFromXp(xp)
        return xp - thresholds.getOrElse(level - 1) { 0 }
    }

    fun xpNeededInLevel(xp: Int): Int {
        val level = levelFromXp(xp)
        val start = thresholds.getOrElse(level - 1) { 0 }
        val end = thresholds.getOrElse(level) { Int.MAX_VALUE }
        return if (end == Int.MAX_VALUE) 0 else end - start
    }
}

// ─────────────────────────────────────────────────────────────
// UserStatistics data model
// ─────────────────────────────────────────────────────────────
data class UserStatistics(
    var username: String = "",
    var totalXp: Int = 0,
    var profilePictureUri: String? = null,  // local URI from camera/gallery

    // Singleplayer
    var singleplayerWins: Int = 0,
    var singleplayerLosses: Int = 0,
    var singleplayerTies: Int = 0,

    // Local Multiplayer — Red
    var localMultiplayerWinsRed: Int = 0,
    var localMultiplayerLossesRed: Int = 0,
    var localMultiplayerTiesRed: Int = 0,

    // Local Multiplayer — Blue
    var localMultiplayerWinsBlue: Int = 0,
    var localMultiplayerLossesBlue: Int = 0,
    var localMultiplayerTiesBlue: Int = 0,

    // Online Multiplayer
    var onlineMultiplayerWins: Int = 0,
    var onlineMultiplayerLosses: Int = 0,
    var onlineMultiplayerTies: Int = 0
) {
    fun getTotalSingleplayerGames() = singleplayerWins + singleplayerLosses + singleplayerTies
    fun getTotalLocalMultiplayerGamesRed() = localMultiplayerWinsRed + localMultiplayerLossesRed + localMultiplayerTiesRed
    fun getTotalLocalMultiplayerGamesBlue() = localMultiplayerWinsBlue + localMultiplayerLossesBlue + localMultiplayerTiesBlue
    fun getTotalOnlineMultiplayerGames() = onlineMultiplayerWins + onlineMultiplayerLosses + onlineMultiplayerTies
    fun getTotalGames() = getTotalSingleplayerGames() + getTotalLocalMultiplayerGamesRed() + getTotalOnlineMultiplayerGames()

    // Level helpers
    fun getLevel() = LevelSystem.levelFromXp(totalXp)
    fun getLevelProgress() = LevelSystem.levelProgress(totalXp)
    fun getXpInCurrentLevel() = LevelSystem.xpInCurrentLevel(totalXp)
    fun getXpNeededInLevel() = LevelSystem.xpNeededInLevel(totalXp)

    // XP awards
    fun awardWin()  { totalXp += XpValues.WIN + XpValues.GAME_PLAYED }
    fun awardLoss() { totalXp += XpValues.LOSS + XpValues.GAME_PLAYED }
    fun awardTie()  { totalXp += XpValues.TIE + XpValues.GAME_PLAYED }
}

// ─────────────────────────────────────────────────────────────
// UserStatsManager — in-memory singleton + Firestore persistence
// ─────────────────────────────────────────────────────────────
object UserStatsManager {
    private var currentStats: UserStatistics? = null
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val uid get() = auth.currentUser?.uid

    // Called on login — loads from Firestore or creates fresh
    suspend fun loadOrInitialize(username: String) {
        val uid = uid ?: run {
            currentStats = UserStatistics(username = username)
            return
        }
        try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val data = doc.data ?: emptyMap()
                currentStats = UserStatistics(
                    username = data["username"] as? String ?: username,
                    totalXp = (data["totalXp"] as? Long)?.toInt() ?: 0,
                    profilePictureUri = data["profilePictureUri"] as? String,
                    singleplayerWins = (data["singleplayerWins"] as? Long)?.toInt() ?: 0,
                    singleplayerLosses = (data["singleplayerLosses"] as? Long)?.toInt() ?: 0,
                    singleplayerTies = (data["singleplayerTies"] as? Long)?.toInt() ?: 0,
                    localMultiplayerWinsRed = (data["localMultiplayerWinsRed"] as? Long)?.toInt() ?: 0,
                    localMultiplayerLossesRed = (data["localMultiplayerLossesRed"] as? Long)?.toInt() ?: 0,
                    localMultiplayerTiesRed = (data["localMultiplayerTiesRed"] as? Long)?.toInt() ?: 0,
                    localMultiplayerWinsBlue = (data["localMultiplayerWinsBlue"] as? Long)?.toInt() ?: 0,
                    localMultiplayerLossesBlue = (data["localMultiplayerLossesBlue"] as? Long)?.toInt() ?: 0,
                    localMultiplayerTiesBlue = (data["localMultiplayerTiesBlue"] as? Long)?.toInt() ?: 0,
                    onlineMultiplayerWins = (data["onlineMultiplayerWins"] as? Long)?.toInt() ?: 0,
                    onlineMultiplayerLosses = (data["onlineMultiplayerLosses"] as? Long)?.toInt() ?: 0,
                    onlineMultiplayerTies = (data["onlineMultiplayerTies"] as? Long)?.toInt() ?: 0
                )
            } else {
                currentStats = UserStatistics(username = username)
                saveToFirestore()
            }
        } catch (e: Exception) {
            currentStats = UserStatistics(username = username)
        }
    }

    // Keep old synchronous init for backward compatibility
    fun initializeStats(username: String) {
        currentStats = UserStatistics(username = username)
    }

    fun getStats(): UserStatistics? = currentStats

    fun updateStats(update: (UserStatistics) -> Unit) {
        currentStats?.let { update(it) }
    }

    // Update username in-memory + Firestore (if online).
    // Called by ProfileView after DeckRepository.saveUsername() already persisted locally.
    suspend fun updateUsername(newUsername: String): Result<Unit> {
        return try {
            currentStats = currentStats?.copy(username = newUsername)
            // Only sync to Firebase if a user is signed in (non-offline mode)
            val currentUid = uid
            if (currentUid != null) {
                val profileUpdates = userProfileChangeRequest { displayName = newUsername }
                auth.currentUser?.updateProfile(profileUpdates)?.await()
                db.collection("users").document(currentUid)
                    .set(mapOf("username" to newUsername),
                        com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Still succeeded locally — don't surface Firestore errors to ProfileView
            Result.success(Unit)
        }
    }

    // Suspend so ProfileView can await and refresh UI after
    suspend fun updateProfilePicture(uri: String): Result<Unit> {
        return try {
            currentStats = currentStats?.copy(profilePictureUri = uri)
            uid?.let {
                db.collection("users").document(it)
                    .set(mapOf("profilePictureUri" to uri),
                        com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)  // saved in memory even if Firestore fails
        }
    }

    // Always returns a fresh copy so Compose sees a new object and recomposes
    fun getStatsCopy(): UserStatistics? = currentStats?.copy()

    // Save all stats to Firestore
    suspend fun saveToFirestore() {
        val stats = currentStats ?: return
        val uid = uid ?: return
        try {
            db.collection("users").document(uid).set(
                mapOf(
                    "username" to stats.username,
                    "totalXp" to stats.totalXp,
                    "profilePictureUri" to stats.profilePictureUri,
                    "singleplayerWins" to stats.singleplayerWins,
                    "singleplayerLosses" to stats.singleplayerLosses,
                    "singleplayerTies" to stats.singleplayerTies,
                    "localMultiplayerWinsRed" to stats.localMultiplayerWinsRed,
                    "localMultiplayerLossesRed" to stats.localMultiplayerLossesRed,
                    "localMultiplayerTiesRed" to stats.localMultiplayerTiesRed,
                    "localMultiplayerWinsBlue" to stats.localMultiplayerWinsBlue,
                    "localMultiplayerLossesBlue" to stats.localMultiplayerLossesBlue,
                    "localMultiplayerTiesBlue" to stats.localMultiplayerTiesBlue,
                    "onlineMultiplayerWins" to stats.onlineMultiplayerWins,
                    "onlineMultiplayerLosses" to stats.onlineMultiplayerLosses,
                    "onlineMultiplayerTies" to stats.onlineMultiplayerTies
                )
            ).await()
        } catch (e: Exception) {
            // Silently fail — stats are still in memory
        }
    }
}