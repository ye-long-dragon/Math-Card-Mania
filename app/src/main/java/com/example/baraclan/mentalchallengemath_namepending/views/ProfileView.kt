package com.example.baraclan.mentalchallengemath_namepending.views

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.baraclan.mentalchallengemath_namepending.data.DeckRepository
import com.example.baraclan.mentalchallengemath_namepending.models.LevelSystem
import com.example.baraclan.mentalchallengemath_namepending.models.UserStatsManager
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileView(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // ── Source of truth: DeckRepository flows ─────────────────
    val savedUsername by DeckRepository.getUsernameFlow(context)
        .collectAsState(initial = "")
    val savedPhotoUri by DeckRepository.getPhotoUriFlow(context)
        .collectAsState(initial = null)

    // ── XP / stats still come from UserStatsManager ───────────
    var stats by remember { mutableStateOf(UserStatsManager.getStatsCopy()) }
    LaunchedEffect(Unit) { stats = UserStatsManager.getStatsCopy() }

    // ── Sync UserStatsManager username whenever DeckRepository changes ──
    // (so in-memory stats object stays consistent)
    LaunchedEffect(savedUsername) {
        if (savedUsername.isNotBlank() &&
            savedUsername != UserStatsManager.getStats()?.username) {
            UserStatsManager.updateUsername(savedUsername)
            stats = UserStatsManager.getStatsCopy()
        }
    }

    // ── Username editing ──────────────────────────────────────
    var isEditingUsername by remember { mutableStateOf(false) }
    var usernameInput     by remember { mutableStateOf("") }
    var usernameSaving    by remember { mutableStateOf(false) }
    var usernameError     by remember { mutableStateOf<String?>(null) }

    // Keep input in sync when not editing
    LaunchedEffect(savedUsername) {
        if (!isEditingUsername) usernameInput = savedUsername
    }

    // ── Photo dialog ──────────────────────────────────────────
    var showPhotoDialog by remember { mutableStateOf(false) }

    val profileUri: Uri? = savedPhotoUri?.let { Uri.parse(it) }

    val cameraUri = remember {
        val file = File(context.cacheDir, "profile_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            scope.launch {
                // Save to DeckRepository (local) + keep UserStatsManager in sync
                DeckRepository.savePhotoUri(context, cameraUri.toString())
                UserStatsManager.updateProfilePicture(cameraUri.toString())
                stats = UserStatsManager.getStatsCopy()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                DeckRepository.savePhotoUri(context, it.toString())
                UserStatsManager.updateProfilePicture(it.toString())
                stats = UserStatsManager.getStatsCopy()
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge,
            fontFamily = Pixel, color = BlackBoardYellow)

        Spacer(modifier = Modifier.height(24.dp))

        // ── Profile picture ───────────────────────────────────
        Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.BottomEnd) {
            if (profileUri != null) {
                AsyncImage(
                    model = profileUri,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, BlackBoardYellow, CircleShape)
                        .clickable { showPhotoDialog = true }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(BlackBoardYellow.copy(alpha = 0.2f))
                        .border(2.dp, BlackBoardYellow, CircleShape)
                        .clickable { showPhotoDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", fontSize = 48.sp, color = BlackBoardYellow)
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change photo",
                    tint = BlackBoardYellow, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Username row ──────────────────────────────────────
        if (isEditingUsername) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.take(20) },
                    singleLine = true,
                    label = { Text("Username", fontFamily = Pixel) },
                    modifier = Modifier.weight(1f),
                    isError = usernameError != null,
                    supportingText = usernameError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error, fontFamily = Pixel) }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (usernameSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp),
                        color = BlackBoardYellow, strokeWidth = 2.dp)
                } else {
                    // ✓ Save — writes to DeckRepository + syncs UserStatsManager
                    IconButton(onClick = {
                        val trimmed = usernameInput.trim()
                        if (trimmed.isBlank()) {
                            usernameError = "Username cannot be empty"
                            return@IconButton
                        }
                        usernameSaving = true
                        usernameError = null
                        scope.launch {
                            // 1. Persist locally via DeckRepository
                            DeckRepository.saveUsername(context, trimmed)
                            // 2. Sync to UserStatsManager in-memory + Firestore
                            UserStatsManager.updateUsername(trimmed)
                            stats = UserStatsManager.getStatsCopy()
                            usernameSaving = false
                            isEditingUsername = false
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.Green)
                    }
                    IconButton(onClick = {
                        usernameInput = savedUsername
                        usernameError = null
                        isEditingUsername = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red)
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = savedUsername.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Pixel, color = BlackBoardYellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    usernameInput = savedUsername
                    isEditingUsername = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit username",
                        tint = BlackBoardYellow, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Level + XP bar ────────────────────────────────────
        stats?.let { s ->
            val level    = s.getLevel()
            val progress = s.getLevelProgress()
            val xpIn     = s.getXpInCurrentLevel()
            val xpNeeded = s.getXpNeededInLevel()

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlackBoardYellow.copy(alpha = 0.12f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Level $level", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 22.sp)
                    Text("${s.totalXp} XP total", fontFamily = Pixel,
                        color = BlackBoardYellow.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = BlackBoardYellow, trackColor = BlackBoardYellow.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (level < LevelSystem.maxLevel) {
                    Text("$xpIn / $xpNeeded XP to Level ${level + 1}",
                        fontFamily = Pixel, color = BlackBoardYellow.copy(alpha = 0.7f),
                        fontSize = 11.sp, textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth())
                } else {
                    Text("Max Level Reached!", fontFamily = Pixel, color = BlackBoardYellow,
                        fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Stats cards ───────────────────────────────────────
        stats?.let { s ->
            StatsCard("Singleplayer", s.singleplayerWins, s.singleplayerLosses,
                s.singleplayerTies, s.getTotalSingleplayerGames())
            Spacer(modifier = Modifier.height(12.dp))
            StatsCard("Local Multiplayer — Red", s.localMultiplayerWinsRed,
                s.localMultiplayerLossesRed, s.localMultiplayerTiesRed,
                s.getTotalLocalMultiplayerGamesRed())
            Spacer(modifier = Modifier.height(12.dp))
            StatsCard("Local Multiplayer — Blue", s.localMultiplayerWinsBlue,
                s.localMultiplayerLossesBlue, s.localMultiplayerTiesBlue,
                s.getTotalLocalMultiplayerGamesBlue())
            Spacer(modifier = Modifier.height(12.dp))
            StatsCard("Online Multiplayer", s.onlineMultiplayerWins,
                s.onlineMultiplayerLosses, s.onlineMultiplayerTies,
                s.getTotalOnlineMultiplayerGames())
        } ?: Text("No profile data available", fontFamily = Pixel,
            color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateBack) {
            Text("Back to Menu", fontFamily = Pixel, color = BlackBoardYellow)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // ── Photo source dialog ───────────────────────────────────
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Change Profile Picture", fontFamily = Pixel, color = BlackBoardYellow) },
            text = { Text("Choose a source", fontFamily = Pixel, color = BlackBoardYellow) },
            confirmButton = {
                TextButton(onClick = { showPhotoDialog = false; cameraLauncher.launch(cameraUri) }) {
                    Text("📷  Camera", fontFamily = Pixel, color = BlackBoardYellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false; galleryLauncher.launch("image/*") }) {
                    Text("🖼  Gallery", fontFamily = Pixel, color = BlackBoardYellow)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable stats card
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatsCard(title: String, wins: Int, losses: Int, ties: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontFamily = Pixel, color = BlackBoardYellow)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatChip("W", wins, Color(0xFF4CAF50))
                StatChip("L", losses, Color(0xFFF44336))
                StatChip("T", ties, Color(0xFFFF9800))
                StatChip("Total", total, BlackBoardYellow)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontFamily = Pixel, color = color, fontSize = 20.sp)
        Text(label, fontFamily = Pixel, color = color.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}