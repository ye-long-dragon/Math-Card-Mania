package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.data.GameMode
import com.example.baraclan.mentalchallengemath_namepending.data.LobbyPlayer
import com.example.baraclan.mentalchallengemath_namepending.data.LobbySettings
import com.example.baraclan.mentalchallengemath_namepending.data.LobbyStatus
import com.example.baraclan.mentalchallengemath_namepending.data.OnlineLobby
import com.example.baraclan.mentalchallengemath_namepending.data.OnlineLobbyRepository
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// OnlineLobbyScreen
// Handles both HOST (creates lobby) and PLAYER (joins lobby)
// ─────────────────────────────────────────────────────────────
@Composable
fun OnlineLobbyScreen(
    lobbyIdArg: String,              // "HOST_NEW" → create, otherwise join
    onGameStart: (lobbyId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var lobbyId by remember { mutableStateOf<String?>(if (lobbyIdArg != "HOST_NEW") lobbyIdArg else null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(lobbyIdArg == "HOST_NEW") }

    // Default settings for new lobby
    var settings by remember { mutableStateOf(LobbySettings()) }

    // Create lobby if hosting
    LaunchedEffect(Unit) {
        if (lobbyIdArg == "HOST_NEW") {
            val result = OnlineLobbyRepository.createLobby(settings)
            result.onSuccess { lobby ->
                lobbyId = lobby.lobbyId
                isCreating = false
            }.onFailure { e ->
                errorMessage = e.message
                isCreating = false
            }
        }
    }

    if (isCreating) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = BlackBoardYellow)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Creating lobby...", fontFamily = Pixel, color = BlackBoardYellow)
            }
        }
        return
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontFamily = Pixel, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }
        }
        return
    }

    val currentLobbyId = lobbyId ?: return

    // Listen to live lobby state
    val lobby by OnlineLobbyRepository.listenToLobby(currentLobbyId)
        .collectAsState(initial = null)

    // Navigate to game when host starts
    LaunchedEffect(lobby?.status) {
        if (lobby?.status == LobbyStatus.IN_GAME) {
            onGameStart(currentLobbyId)
        }
    }

    val currentUid = OnlineLobbyRepository.currentUid
    val isHost = lobby?.hostUid == currentUid
    val players = lobby?.players?.values?.toList() ?: emptyList()
    val allNonHostReady = players.filter { !it.isHost }.all { it.isReady }
    val canStart = allNonHostReady && players.size >= 2

    lobby?.let { currentLobby ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ── Header ────────────────────────────────────────
            Text(
                text = if (isHost) "Your Lobby" else "Waiting for Host",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = Pixel,
                    color = BlackBoardYellow,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // ── Join Code ─────────────────────────────────────
            JoinCodeDisplay(code = currentLobby.joinCode)

            Spacer(modifier = Modifier.height(12.dp))

            // ── Settings (host only editable) ─────────────────
            if (isHost) {
                LobbySettingsPanel(
                    settings = currentLobby.settings,
                    onSettingsChanged = { newSettings ->
                        scope.launch {
                            OnlineLobbyRepository.updateSettings(currentLobbyId, newSettings)
                        }
                    }
                )
            } else {
                LobbySettingsDisplay(settings = currentLobby.settings)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Player list ───────────────────────────────────
            Text(
                text = "Players (${players.size}/${currentLobby.settings.maxPlayers})",
                fontFamily = Pixel,
                color = BlackBoardYellow,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(players, key = { it.uid }) { player ->
                    PlayerLobbyRow(player = player, isCurrentUser = player.uid == currentUid)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Action buttons ────────────────────────────────
            if (isHost) {
                if (!canStart) {
                    Text(
                        text = if (players.size < 2) "Waiting for players to join..."
                        else "Waiting for all players to ready up...",
                        fontFamily = Pixel,
                        color = BlackBoardYellow.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            val goals = generateGameGoals()
                            OnlineLobbyRepository.startGame(currentLobbyId, goals)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = canStart
                ) {
                    Text("START GAME", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 18.sp)
                }
            } else {
                val myPlayer = players.firstOrNull { it.uid == currentUid }
                val isReady = myPlayer?.isReady ?: false
                Button(
                    onClick = {
                        scope.launch {
                            OnlineLobbyRepository.setReady(currentLobbyId, !isReady)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReady) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isReady) "✓ Ready! (tap to unready)" else "Ready Up",
                        fontFamily = Pixel,
                        color = BlackBoardYellow,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        OnlineLobbyRepository.leaveLobby(currentLobbyId)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Leave Lobby", fontFamily = Pixel, color = MaterialTheme.colorScheme.error)
            }
        }
    } ?: run {
        // Lobby was deleted (host left)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lobby closed.", fontFamily = Pixel, color = BlackBoardYellow)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Join Code display
// ─────────────────────────────────────────────────────────────
@Composable
fun JoinCodeDisplay(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlackBoardYellow.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Join Code",
            fontFamily = Pixel,
            color = BlackBoardYellow.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = code,
            fontFamily = Pixel,
            color = BlackBoardYellow,
            fontSize = 36.sp,
            letterSpacing = 8.sp
        )
        Text(
            text = "Share this with friends",
            fontFamily = Pixel,
            color = BlackBoardYellow.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Settings panel (editable by host)
// ─────────────────────────────────────────────────────────────
@Composable
fun LobbySettingsPanel(
    settings: LobbySettings,
    onSettingsChanged: (LobbySettings) -> Unit
) {
    var localSettings by remember(settings) { mutableStateOf(settings) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlackBoardYellow.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(
            "Game Settings",
            fontFamily = Pixel,
            color = BlackBoardYellow,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Public / Private toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Public lobby", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = localSettings.isPublic,
                onCheckedChange = {
                    localSettings = localSettings.copy(isPublic = it)
                    onSettingsChanged(localSettings)
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Max players slider
        Text("Max players: ${localSettings.maxPlayers}", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp)
        Slider(
            value = localSettings.maxPlayers.toFloat(),
            onValueChange = {
                localSettings = localSettings.copy(maxPlayers = it.toInt())
            },
            onValueChangeFinished = { onSettingsChanged(localSettings) },
            valueRange = 2f..8f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Game mode
        Text("Game mode", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(GameMode.STOPWATCH to "⏱ Stopwatch", GameMode.TIMED_ROUND to "⏰ Timed Round")
                .forEach { (mode, label) ->
                    FilterChip(
                        selected = localSettings.gameMode == mode,
                        onClick = {
                            localSettings = localSettings.copy(gameMode = mode)
                            onSettingsChanged(localSettings)
                        },
                        label = { Text(label, fontFamily = Pixel, fontSize = 11.sp) }
                    )
                }
        }

        // Round timer (only for TIMED_ROUND)
        if (localSettings.gameMode == GameMode.TIMED_ROUND) {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Round time: ${localSettings.roundTimerSeconds}s", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp)
            Slider(
                value = localSettings.roundTimerSeconds.toFloat(),
                onValueChange = { localSettings = localSettings.copy(roundTimerSeconds = it.toInt()) },
                onValueChangeFinished = { onSettingsChanged(localSettings) },
                valueRange = 15f..120f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Read-only settings view for non-host players
@Composable
fun LobbySettingsDisplay(settings: LobbySettings) {
    val modeText = if (settings.gameMode == GameMode.STOPWATCH) "⏱ Stopwatch" else "⏰ Timed (${settings.roundTimerSeconds}s/round)"
    val visText = if (settings.isPublic) "🌐 Public" else "🔒 Private"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlackBoardYellow.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(visText, fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp)
        Text(modeText, fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp)
        Text("Max: ${settings.maxPlayers}", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 13.sp)
    }
}

// ─────────────────────────────────────────────────────────────
// Player row in lobby
// ─────────────────────────────────────────────────────────────
@Composable
fun PlayerLobbyRow(player: LobbyPlayer, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrentUser) BlackBoardYellow.copy(alpha = 0.2f)
                else BlackBoardYellow.copy(alpha = 0.08f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = BlackBoardYellow,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = player.username + if (isCurrentUser) " (you)" else "",
            fontFamily = Pixel,
            color = BlackBoardYellow,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (player.isHost) {
            Text("HOST", fontFamily = Pixel, color = BlackBoardYellow.copy(alpha = 0.7f), fontSize = 11.sp)
        } else if (player.isReady) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = Color.Green, modifier = Modifier.size(20.dp))
        } else {
            Text("not ready", fontFamily = Pixel, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helper: generate 5 game goals (shared across all players)
// ─────────────────────────────────────────────────────────────
fun generateGameGoals(): List<Double> {
    return (1..5).map { (1..20).random().toDouble() }
}