package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.data.GameMode
import com.example.baraclan.mentalchallengemath_namepending.data.OnlineLobby
import com.example.baraclan.mentalchallengemath_namepending.data.OnlineLobbyRepository
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// Entry screen: Host / Join by Code / Public Lobbies
// ─────────────────────────────────────────────────────────────
@Composable
fun OnlineMultiplayerEntryScreen(
    onNavigateToLobby: (lobbyId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // 0 = main menu, 1 = join by code, 2 = public lobbies
    var screen by remember { mutableStateOf(0) }

    when (screen) {
        0 -> EntryMenuScreen(
            onHost = { onNavigateToLobby("HOST_NEW") },
            onJoinByCode = { screen = 1 },
            onPublicLobbies = { screen = 2 },
            onBack = onNavigateBack
        )
        1 -> JoinByCodeScreen(
            onJoined = { lobbyId -> onNavigateToLobby(lobbyId) },
            onBack = { screen = 0 }
        )
        2 -> PublicLobbiesScreen(
            onJoined = { lobbyId -> onNavigateToLobby(lobbyId) },
            onBack = { screen = 0 }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Main entry menu
// ─────────────────────────────────────────────────────────────
@Composable
fun EntryMenuScreen(
    onHost: () -> Unit,
    onJoinByCode: () -> Unit,
    onPublicLobbies: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Online Multiplayer",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OnlineMenuButton("🏠  Host a Game", onClick = onHost)
        Spacer(modifier = Modifier.height(16.dp))
        OnlineMenuButton("🔑  Join by Code", onClick = onJoinByCode)
        Spacer(modifier = Modifier.height(16.dp))
        OnlineMenuButton("🌐  Browse Public Lobbies", onClick = onPublicLobbies)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
        }
    }
}

@Composable
private fun OnlineMenuButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(label, fontFamily = FontFamily.Monospace, color = BlackBoardYellow, fontSize = 16.sp)
    }
}

// ─────────────────────────────────────────────────────────────
// Join by Code screen
// ─────────────────────────────────────────────────────────────
@Composable
fun JoinByCodeScreen(
    onJoined: (lobbyId: String) -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter Join Code",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase().take(6) },
            label = { Text("6-character code", fontFamily = FontFamily.Monospace) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp
            )
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (code.length != 6) {
                    errorMessage = "Code must be 6 characters"
                    return@Button
                }
                isLoading = true
                errorMessage = null
                scope.launch {
                    val result = OnlineLobbyRepository.joinLobbyByCode(code)
                    isLoading = false
                    result
                        .onSuccess { lobbyId -> onJoined(lobbyId) }
                        .onFailure { e -> errorMessage = e.message }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && code.length == 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BlackBoardYellow,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Join Game", fontFamily = FontFamily.Monospace, color = BlackBoardYellow, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Public Lobbies screen
// ─────────────────────────────────────────────────────────────
@Composable
fun PublicLobbiesScreen(
    onJoined: (lobbyId: String) -> Unit,
    onBack: () -> Unit
) {
    val publicLobbies by OnlineLobbyRepository.listenToPublicLobbies()
        .collectAsState(initial = emptyList())
    var joiningId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Public Lobbies",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (publicLobbies.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No public lobbies right now.",
                        fontFamily = FontFamily.Monospace,
                        color = BlackBoardYellow,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Why not host one?",
                        fontFamily = FontFamily.Monospace,
                        color = BlackBoardYellow.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Table header
            PublicLobbyHeader()
            HorizontalDivider(color = BlackBoardYellow.copy(alpha = 0.3f))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(publicLobbies, key = { it.lobbyId }) { lobby ->
                    PublicLobbyRow(
                        lobby = lobby,
                        isJoining = joiningId == lobby.lobbyId,
                        onJoin = {
                            joiningId = lobby.lobbyId
                            errorMessage = null
                            scope.launch {
                                val result = OnlineLobbyRepository.joinLobbyById(lobby.lobbyId)
                                joiningId = null
                                result
                                    .onSuccess { onJoined(lobby.lobbyId) }
                                    .onFailure { e -> errorMessage = e.message }
                            }
                        }
                    )
                    HorizontalDivider(color = BlackBoardYellow.copy(alpha = 0.15f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
        }
    }
}

@Composable
private fun PublicLobbyHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Host", fontFamily = FontFamily.Monospace, color = BlackBoardYellow.copy(alpha = 0.7f),
            fontSize = 12.sp, modifier = Modifier.weight(2f))
        Text("Players", fontFamily = FontFamily.Monospace, color = BlackBoardYellow.copy(alpha = 0.7f),
            fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text("Mode", fontFamily = FontFamily.Monospace, color = BlackBoardYellow.copy(alpha = 0.7f),
            fontSize = 12.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun PublicLobbyRow(
    lobby: OnlineLobby,
    isJoining: Boolean,
    onJoin: () -> Unit
) {
    val host = lobby.players.values.firstOrNull { it.isHost }
    val playerCount = lobby.players.size
    val isFull = playerCount >= lobby.settings.maxPlayers
    val modeLabel = if (lobby.settings.gameMode == GameMode.STOPWATCH) "⏱ Speed" else "⏰ Timed"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = host?.username ?: "Unknown",
            fontFamily = FontFamily.Monospace,
            color = BlackBoardYellow,
            fontSize = 13.sp,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "$playerCount/${lobby.settings.maxPlayers}",
            fontFamily = FontFamily.Monospace,
            color = if (isFull) MaterialTheme.colorScheme.error else BlackBoardYellow,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = modeLabel,
            fontFamily = FontFamily.Monospace,
            color = BlackBoardYellow,
            fontSize = 12.sp,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onJoin,
            enabled = !isFull && !isJoining,
            modifier = Modifier.weight(1.5f).height(36.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            if (isJoining) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BlackBoardYellow)
            } else {
                Text(if (isFull) "Full" else "Join", fontFamily = FontFamily.Monospace,
                    color = BlackBoardYellow, fontSize = 12.sp)
            }
        }
    }
}