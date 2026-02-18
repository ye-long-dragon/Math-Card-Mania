package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // Required for displaying drawables
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview // For previewing your composables
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.* // This imports your models, including Deck
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow // Assuming this color exists in your theme
import com.example.baraclan.mentalchallengemath_namepending.R // Required to access your drawable resources

// --- Start of assumed `com.example.baraclan.mentalchallengemath_namepending.models.Deck` definition ---
// You should ideally place this in a separate file like `app/src/main/java/com/example/baraclan/mentalchallengemath_namepending/models/Deck.kt`
// to align with the package structure.
// For demonstration, I'm including it here.

import androidx.annotation.DrawableRes

data class Deck(
    val id: String,
    val name: String,
    @DrawableRes val imageResId: Int // Resource ID for the deck's image
)
// --- End of assumed `com.example.baraclan.mentalchallengemath_namepending.models.Deck` definition ---

@Composable
fun DeckSelectScreen(
    // A callback function to handle when a deck is selected, e.g., for navigation
    onDeckSelected: (Deck) -> Unit = {}
) {
    // Dummy data for demonstration. In a real application, this would typically
    // come from a ViewModel, database, or a repository.
    val sampleDecks = listOf(
        Deck("starting_deck", "Starting Deck", R.drawable.ic_launcher_foreground), // Use your actual drawable here

    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        Text(
            text = "Choose your Deck",
            // Apply MaterialTheme typography for consistent styling
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Use LazyColumn for efficient rendering of a scrollable list of decks
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
        ) {
            items(sampleDecks) { deck ->
                DeckTile(deck = deck, onDeckClick = onDeckSelected)
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Space before the button
        Button(
            onClick = { /* TODO: Implement navigation to an "Add New Deck" screen */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Deck")
        }
    }
}

@Composable
fun DeckTile(deck: Deck, onDeckClick: (Deck) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Make the card slightly narrower than full width
            .height(140.dp) // Fixed height for consistent look
            .clickable { onDeckClick(deck) }, // Make the card clickable
        shape = MaterialTheme.shapes.medium, // Rounded corners
        colors = CardDefaults.cardColors(
            containerColor = BlackBoardYellow // Use your custom color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Add some shadow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Center image and text horizontally
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            // Display the deck image from resources
            Image(
                painter = painterResource(id = deck.imageResId),
                contentDescription = "Deck image for ${deck.name}",
                modifier = Modifier
                    .size(80.dp) // Adjust image size
                    .padding(bottom = 8.dp)
            )
            // Display the deck name
            Text(
                text = deck.name,
                style = MaterialTheme.typography.titleMedium, // Apply MaterialTheme typography
                color = Color.Black, // Text color for contrast with BlackBoardYellow
                fontFamily = FontFamily.SansSerif, // Example font family
                textAlign = TextAlign.Center
            )
        }
    }
}

