package com.example.baraclan.mentalchallengemath_namepending.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────
// Data models
// ─────────────────────────────────────────────────────────────

enum class GameMode { STOPWATCH, TIMED_ROUND }
enum class LobbyStatus { WAITING, IN_GAME, FINISHED }

data class LobbyPlayer(
    val uid: String = "",
    val username: String = "",
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    val finishTimeSeconds: Int? = null,   // null = not finished yet
    val score: Int = 0
)

data class LobbySettings(
    val isPublic: Boolean = true,
    val maxPlayers: Int = 4,
    val gameMode: GameMode = GameMode.STOPWATCH,
    val roundTimerSeconds: Int = 60       // only used if TIMED_ROUND
)

data class OnlineLobby(
    val lobbyId: String = "",
    val joinCode: String = "",
    val hostUid: String = "",
    val settings: LobbySettings = LobbySettings(),
    val players: Map<String, LobbyPlayer> = emptyMap(),
    val status: LobbyStatus = LobbyStatus.WAITING,
    val gameGoals: List<Double> = emptyList()
)

// ─────────────────────────────────────────────────────────────
// OnlineLobbyRepository
// ─────────────────────────────────────────────────────────────
object OnlineLobbyRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val lobbiesRef = db.collection("lobbies")

    val currentUid: String get() = auth.currentUser?.uid ?: ""
    val currentUsername: String get() = auth.currentUser?.displayName
        ?: auth.currentUser?.email?.substringBefore("@") ?: "Player"

    // ── Generate a random 6-character join code ───────────────
    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    // ── HOST: Create a new lobby ──────────────────────────────
    suspend fun createLobby(settings: LobbySettings): Result<OnlineLobby> {
        return try {
            val uid = currentUid
            val username = currentUsername
            val joinCode = generateJoinCode()

            val hostPlayer = mapOf(
                uid to mapOf(
                    "uid" to uid,
                    "username" to username,
                    "isReady" to true,   // host is always ready
                    "isHost" to true,
                    "finishTimeSeconds" to null,
                    "score" to 0
                )
            )

            val lobbyData = hashMapOf(
                "joinCode" to joinCode,
                "hostUid" to uid,
                "isPublic" to settings.isPublic,
                "maxPlayers" to settings.maxPlayers,
                "gameMode" to settings.gameMode.name,
                "roundTimerSeconds" to settings.roundTimerSeconds,
                "players" to hostPlayer,
                "status" to LobbyStatus.WAITING.name,
                "gameGoals" to emptyList<Double>()
            )

            val docRef = lobbiesRef.add(lobbyData).await()

            val lobby = OnlineLobby(
                lobbyId = docRef.id,
                joinCode = joinCode,
                hostUid = uid,
                settings = settings,
                players = mapOf(uid to LobbyPlayer(uid, username, true, true)),
                status = LobbyStatus.WAITING
            )
            Result.success(lobby)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── JOIN: Join by code ────────────────────────────────────
    suspend fun joinLobbyByCode(code: String): Result<String> {
        return try {
            val query = lobbiesRef
                .whereEqualTo("joinCode", code.uppercase().trim())
                .whereEqualTo("status", LobbyStatus.WAITING.name)
                .get().await()

            if (query.isEmpty)
                return Result.failure(Exception("No active lobby found with that code."))

            val doc = query.documents[0]
            val lobbyId = doc.id
            val maxPlayers = (doc.getLong("maxPlayers") ?: 4).toInt()
            val currentPlayers = (doc.get("players") as? Map<*, *>)?.size ?: 0

            if (currentPlayers >= maxPlayers)
                return Result.failure(Exception("This lobby is full."))

            val uid = currentUid
            val username = currentUsername

            lobbiesRef.document(lobbyId)
                .update("players.$uid", mapOf(
                    "uid" to uid,
                    "username" to username,
                    "isReady" to false,
                    "isHost" to false,
                    "finishTimeSeconds" to null,
                    "score" to 0
                )).await()

            Result.success(lobbyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── JOIN: Join public lobby by ID ─────────────────────────
    suspend fun joinLobbyById(lobbyId: String): Result<String> {
        return try {
            val doc = lobbiesRef.document(lobbyId).get().await()
            if (!doc.exists()) return Result.failure(Exception("Lobby not found."))

            val maxPlayers = (doc.getLong("maxPlayers") ?: 4).toInt()
            val currentPlayers = (doc.get("players") as? Map<*, *>)?.size ?: 0
            if (currentPlayers >= maxPlayers) return Result.failure(Exception("Lobby is full."))

            val uid = currentUid
            val username = currentUsername

            lobbiesRef.document(lobbyId)
                .update("players.$uid", mapOf(
                    "uid" to uid,
                    "username" to username,
                    "isReady" to false,
                    "isHost" to false,
                    "finishTimeSeconds" to null,
                    "score" to 0
                )).await()

            Result.success(lobbyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── LISTEN: Live lobby updates ────────────────────────────
    fun listenToLobby(lobbyId: String): Flow<OnlineLobby?> = callbackFlow {
        val reg: ListenerRegistration = lobbiesRef.document(lobbyId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null || !snap.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(parseLobby(snap.id, snap.data ?: emptyMap()))
            }
        awaitClose { reg.remove() }
    }

    // ── LISTEN: Public lobbies list ───────────────────────────
    fun listenToPublicLobbies(): Flow<List<OnlineLobby>> = callbackFlow {
        val reg: ListenerRegistration = lobbiesRef
            .whereEqualTo("isPublic", true)
            .whereEqualTo("status", LobbyStatus.WAITING.name)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lobbies = snap.documents.mapNotNull { doc ->
                    doc.data?.let { parseLobby(doc.id, it) }
                }
                trySend(lobbies)
            }
        awaitClose { reg.remove() }
    }

    // ── PLAYER: Toggle ready state ────────────────────────────
    suspend fun setReady(lobbyId: String, isReady: Boolean): Result<Unit> {
        return try {
            lobbiesRef.document(lobbyId)
                .update("players.${currentUid}.isReady", isReady)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── HOST: Update settings ─────────────────────────────────
    suspend fun updateSettings(lobbyId: String, settings: LobbySettings): Result<Unit> {
        return try {
            lobbiesRef.document(lobbyId).update(
                mapOf(
                    "isPublic" to settings.isPublic,
                    "maxPlayers" to settings.maxPlayers,
                    "gameMode" to settings.gameMode.name,
                    "roundTimerSeconds" to settings.roundTimerSeconds
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── HOST: Start game ──────────────────────────────────────
    suspend fun startGame(lobbyId: String, goals: List<Double>): Result<Unit> {
        return try {
            lobbiesRef.document(lobbyId).update(
                mapOf(
                    "status" to LobbyStatus.IN_GAME.name,
                    "gameGoals" to goals
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── PLAYER: Submit finish ─────────────────────────────────
    suspend fun submitFinish(lobbyId: String, timeSeconds: Int, score: Int): Result<Unit> {
        return try {
            lobbiesRef.document(lobbyId).update(
                mapOf(
                    "players.${currentUid}.finishTimeSeconds" to timeSeconds,
                    "players.${currentUid}.score" to score
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── HOST: End game + set final status ─────────────────────
    suspend fun endGame(lobbyId: String): Result<Unit> {
        return try {
            lobbiesRef.document(lobbyId)
                .update("status", LobbyStatus.FINISHED.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── LEAVE: Remove player from lobby ───────────────────────
    suspend fun leaveLobby(lobbyId: String): Result<Unit> {
        return try {
            val uid = currentUid
            val doc = lobbiesRef.document(lobbyId).get().await()
            val players = (doc.get("players") as? Map<*, *>) ?: emptyMap<String, Any>()

            if (players.size <= 1) {
                // Last person — delete the lobby
                lobbiesRef.document(lobbyId).delete().await()
            } else {
                lobbiesRef.document(lobbyId)
                    .update("players.$uid", com.google.firebase.firestore.FieldValue.delete())
                    .await()
                // If host left, promote next player
                val hostUid = doc.getString("hostUid")
                if (hostUid == uid) {
                    val nextUid = players.keys.firstOrNull { it != uid }?.toString()
                    if (nextUid != null) {
                        lobbiesRef.document(lobbyId).update(
                            mapOf(
                                "hostUid" to nextUid,
                                "players.$nextUid.isHost" to true
                            )
                        ).await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Parse Firestore document → OnlineLobby ────────────────
    @Suppress("UNCHECKED_CAST")
    private fun parseLobby(id: String, data: Map<String, Any>): OnlineLobby {
        val playersRaw = data["players"] as? Map<String, Map<String, Any>> ?: emptyMap()
        val players = playersRaw.mapValues { (_, p) ->
            LobbyPlayer(
                uid = p["uid"] as? String ?: "",
                username = p["username"] as? String ?: "Player",
                isReady = p["isReady"] as? Boolean ?: false,
                isHost = p["isHost"] as? Boolean ?: false,
                finishTimeSeconds = (p["finishTimeSeconds"] as? Long)?.toInt(),
                score = (p["score"] as? Long)?.toInt() ?: 0
            )
        }
        val gameMode = try {
            GameMode.valueOf(data["gameMode"] as? String ?: "STOPWATCH")
        } catch (e: Exception) { GameMode.STOPWATCH }

        val status = try {
            LobbyStatus.valueOf(data["status"] as? String ?: "WAITING")
        } catch (e: Exception) { LobbyStatus.WAITING }

        val goalsRaw = data["gameGoals"] as? List<*> ?: emptyList<Any>()
        val goals = goalsRaw.mapNotNull {
            when (it) {
                is Double -> it
                is Long -> it.toDouble()
                else -> null
            }
        }

        return OnlineLobby(
            lobbyId = id,
            joinCode = data["joinCode"] as? String ?: "",
            hostUid = data["hostUid"] as? String ?: "",
            settings = LobbySettings(
                isPublic = data["isPublic"] as? Boolean ?: true,
                maxPlayers = (data["maxPlayers"] as? Long)?.toInt() ?: 4,
                gameMode = gameMode,
                roundTimerSeconds = (data["roundTimerSeconds"] as? Long)?.toInt() ?: 60
            ),
            players = players,
            status = status,
            gameGoals = goals
        )
    }
}