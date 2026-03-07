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
import com.example.baraclan.mentalchallengemath_namepending.models.LevelSystem
import com.example.baraclan.mentalchallengemath_namepending.models.UserStatsManager
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileView(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf(UserStatsManager.getStats()) }

    // ── Username editing ──────────────────────────────────────
    var isEditingUsername by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf(stats?.username ?: "") }
    var usernameSaving by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    // ── Photo picker ──────────────────────────────────────────
    var showPhotoDialog by remember { mutableStateOf(false) }
    var profileUri by remember { mutableStateOf<Uri?>(
        stats?.profilePictureUri?.let { Uri.parse(it) }
    ) }

    // Camera: create a temp file URI for the photo
    val cameraUri = remember {
        val file = File(context.cacheDir, "profile_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profileUri = cameraUri
            UserStatsManager.updateProfilePicture(cameraUri.toString())
            stats = UserStatsManager.getStats()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileUri = it
            UserStatsManager.updateProfilePicture(it.toString())
            stats = UserStatsManager.getStats()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = Pixel,
            color = BlackBoardYellow
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Profile picture ───────────────────────────────────
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
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
            // Camera badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    tint = BlackBoardYellow,
                    modifier = Modifier.size(18.dp)
                )
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
                    supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontFamily = Pixel) } }
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (usernameSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BlackBoardYellow, strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = {
                        if (usernameInput.isBlank()) {
                            usernameError = "Username cannot be empty"
                            return@IconButton
                        }
                        usernameSaving = true
                        usernameError = null
                        scope.launch {
                            val result = UserStatsManager.updateUsername(usernameInput.trim())
                            usernameSaving = false
                            result.onSuccess {
                                stats = UserStatsManager.getStats()
                                isEditingUsername = false
                            }.onFailure {
                                usernameError = "Failed to save. Try again."
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.Green)
                    }
                    IconButton(onClick = {
                        usernameInput = stats?.username ?: ""
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
                    text = stats?.username ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Pixel,
                    color = BlackBoardYellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    usernameInput = stats?.username ?: ""
                    isEditingUsername = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit username", tint = BlackBoardYellow, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Level + XP bar ────────────────────────────────────
        stats?.let { s ->
            val level = s.getLevel()
            val progress = s.getLevelProgress()
            val xpIn = s.getXpInCurrentLevel()
            val xpNeeded = s.getXpNeededInLevel()

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlackBoardYellow.copy(alpha = 0.12f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level $level",
                        fontFamily = Pixel,
                        color = BlackBoardYellow,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "${s.totalXp} XP total",
                        fontFamily = Pixel,
                        color = BlackBoardYellow.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = BlackBoardYellow,
                    trackColor = BlackBoardYellow.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (level < LevelSystem.maxLevel) {
                    Text(
                        text = "$xpIn / $xpNeeded XP to Level ${level + 1}",
                        fontFamily = Pixel,
                        color = BlackBoardYellow.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Max Level Reached!",
                        fontFamily = Pixel,
                        color = BlackBoardYellow,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Stats cards ───────────────────────────────────────
        stats?.let { s ->
            StatsCard(
                title = "Singleplayer",
                wins = s.singleplayerWins,
                losses = s.singleplayerLosses,
                ties = s.singleplayerTies,
                total = s.getTotalSingleplayerGames()
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatsCard(
                title = "Local Multiplayer — Red",
                wins = s.localMultiplayerWinsRed,
                losses = s.localMultiplayerLossesRed,
                ties = s.localMultiplayerTiesRed,
                total = s.getTotalLocalMultiplayerGamesRed()
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatsCard(
                title = "Local Multiplayer — Blue",
                wins = s.localMultiplayerWinsBlue,
                losses = s.localMultiplayerLossesBlue,
                ties = s.localMultiplayerTiesBlue,
                total = s.getTotalLocalMultiplayerGamesBlue()
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatsCard(
                title = "Online Multiplayer",
                wins = s.onlineMultiplayerWins,
                losses = s.onlineMultiplayerLosses,
                ties = s.onlineMultiplayerTies,
                total = s.getTotalOnlineMultiplayerGames()
            )
        } ?: run {
            Text("No profile data available", fontFamily = Pixel, color = MaterialTheme.colorScheme.onSurface)
        }

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
                TextButton(onClick = {
                    showPhotoDialog = false
                    cameraLauncher.launch(cameraUri)
                }) {
                    Text("📷  Camera", fontFamily = Pixel, color = BlackBoardYellow)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    galleryLauncher.launch("image/*")
                }) {
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
private fun StatsCard(
    title: String,
    wins: Int,
    losses: Int,
    ties: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontFamily = Pixel, color = BlackBoardYellow)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatChip(label = "W", value = wins, color = Color(0xFF4CAF50))
                StatChip(label = "L", value = losses, color = Color(0xFFF44336))
                StatChip(label = "T", value = ties, color = Color(0xFFFF9800))
                StatChip(label = "Total", value = total, color = BlackBoardYellow)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$value", fontFamily = Pixel, color = color, fontSize = 20.sp)
        Text(text = label, fontFamily = Pixel, color = color.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}