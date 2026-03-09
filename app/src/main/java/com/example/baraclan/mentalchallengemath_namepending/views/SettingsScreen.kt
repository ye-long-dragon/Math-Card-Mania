package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.AudioSettings
import com.example.baraclan.mentalchallengemath_namepending.models.AudioSettingsManager
import com.example.baraclan.mentalchallengemath_namepending.models.MusicManager
import com.example.baraclan.mentalchallengemath_namepending.models.SoundManager
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(AudioSettings()) }

    // Load saved settings on open
    LaunchedEffect(Unit) {
        AudioSettingsManager.getSettingsFlow(context).collect { settings = it }
    }

    fun save(updated: AudioSettings) {
        settings = updated
        MusicManager.applySettings(updated)
        SoundManager.applySettings(updated)
        scope.launch { AudioSettingsManager.save(context, updated) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlackBoardYellow)
            }
            Text(
                text = "Settings",
                fontFamily = Pixel,
                color = BlackBoardYellow,
                fontSize = 26.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Master Volume ─────────────────────────────────────
        SettingsSlider(
            label = "Master Volume",
            value = settings.masterVolume,
            onValueChange = { save(settings.copy(masterVolume = it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Music ─────────────────────────────────────────────
        SettingsToggle(
            label = "Music",
            checked = settings.musicEnabled,
            onCheckedChange = { save(settings.copy(musicEnabled = it)) }
        )
        if (settings.musicEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSlider(
                label = "Music Volume",
                value = settings.musicVolume,
                onValueChange = { save(settings.copy(musicVolume = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── SFX ───────────────────────────────────────────────
        SettingsToggle(
            label = "Sound Effects",
            checked = settings.sfxEnabled,
            onCheckedChange = { save(settings.copy(sfxEnabled = it)) }
        )
        if (settings.sfxEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSlider(
                label = "SFX Volume",
                value = settings.sfxVolume,
                onValueChange = { save(settings.copy(sfxVolume = it)) }
            )
        }
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = Pixel, color = BlackBoardYellow, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontFamily = Pixel, color = BlackBoardYellow, fontSize = 14.sp)
            Text("${(value * 100).toInt()}%", fontFamily = Pixel, color = BlackBoardYellow.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}