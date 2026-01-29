package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp // Added for text size adjustment

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun menu(
    onPlayGameClick: () -> Unit = {},
    onEditDeckClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    // onBackToLogin: () -> Unit // This would typically be handled by a NavController or a global state in the parent
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Play Game Button
            Button(
                onClick = onPlayGameClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Play Game", fontSize = 20.sp)
            }

            // Edit Deck Button
            Button(
                onClick = onEditDeckClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Edit Deck", fontSize = 20.sp)
            }

            // Profile Button
            Button(
                onClick = onProfileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Profile", fontSize = 20.sp)
            }

            // About Button
            Button(
                onClick = onAboutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("About", fontSize = 20.sp)
            }

            // Note: The "when back button (android native) is pressed, returns to login screen"
            // is typically handled by Android's navigation component (NavController) or by managing
            // the back stack in your main activity/navigation host.
            // This specific Composable itself doesn't directly control the system back button behavior,
            // but the `onPlayGameClick`, `onEditDeckClick`, etc., lambdas are where you'd trigger
            // navigation actions to different screens.
        }
    }
}

