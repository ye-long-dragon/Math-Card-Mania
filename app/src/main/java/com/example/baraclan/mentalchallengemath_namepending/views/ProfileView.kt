package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.models.UserStatsManager
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

@Composable
fun ProfileView(
    onNavigateBack: () -> Unit
) {
    val stats = UserStatsManager.getStats()
    
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
        
        if (stats != null) {
            // Username
            Text(
                text = "Username: ${stats.username}",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Pixel,
                color = BlackBoardYellow
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Singleplayer Statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Singleplayer",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Pixel,
                        color = BlackBoardYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wins: ${stats.singleplayerWins}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Losses: ${stats.singleplayerLosses}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ties: ${stats.singleplayerTies}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total Games: ${stats.getTotalSingleplayerGames()}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Local Multiplayer - Player Red
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Local Multiplayer - Player Red",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Pixel,
                        color = BlackBoardYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wins: ${stats.localMultiplayerWinsRed}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Losses: ${stats.localMultiplayerLossesRed}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ties: ${stats.localMultiplayerTiesRed}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total Games: ${stats.getTotalLocalMultiplayerGamesRed()}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Local Multiplayer - Player Blue
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Local Multiplayer - Player Blue",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Pixel,
                        color = BlackBoardYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wins: ${stats.localMultiplayerWinsBlue}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Losses: ${stats.localMultiplayerLossesBlue}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ties: ${stats.localMultiplayerTiesBlue}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total Games: ${stats.getTotalLocalMultiplayerGamesBlue()}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Online Multiplayer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Online Multiplayer",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Pixel,
                        color = BlackBoardYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wins: ${stats.onlineMultiplayerWins}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Losses: ${stats.onlineMultiplayerLosses}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ties: ${stats.onlineMultiplayerTies}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total Games: ${stats.getTotalOnlineMultiplayerGames()}",
                        fontFamily = Pixel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "No profile data available",
                fontFamily = Pixel,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onNavigateBack) {
            Text("Back to Menu", fontFamily = Pixel, color = BlackBoardYellow)
        }
    }
}
