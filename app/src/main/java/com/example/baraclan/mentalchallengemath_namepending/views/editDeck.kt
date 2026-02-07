package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer // Added for spacing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Added for Spacer height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme // Added MaterialTheme import
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily // Added FontFamily import
import androidx.compose.ui.text.style.TextAlign // Added TextAlign import
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Added sp for font sizes
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

// Removed: import com.example.baraclan.mentalchallengemath_namepending.views.* (redundant as this file is in that package)

// Assuming 'Pixel' FontFamily is defined somewhere and imported, e.g.:
// import com.example.baraclan.mentalchallengemath_namepending.ui.theme.Type.Pixel
// If not, you'll need to define it or replace it with a standard font family.
// For demonstration, I'll assume 'Pixel' is imported or available.
// Example definition if not imported:
// val Pixel = FontFamily(androidx.compose.ui.text.font.Font(R.font.your_pixel_font_file))


@Composable
public fun EditDeckScreen(
    cardDeck: deck, // Assuming 'deck' is a class with a 'cards' property: List<cardGame>
    collection: collection, // Assuming 'collection' is a class with a 'cards' property: List<cardGame>
    onReturnMenu: () -> Unit = {},
    onClickDeck: (cardGame) -> Unit = {}, // Callback for clicking a card in the deck
    onClickCollection: (cardGame) -> Unit = {}, // Callback for clicking a card in the collection
) {
    Column(
        modifier = Modifier.fillMaxSize() // This Column takes up the whole screen
    ) {
        // "Edit Deck" Title
        Text(
            text = "Edit Deck",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp), // Adjust padding as needed
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 28.sp, // A bit larger for a main title
                color = BlackBoardYellow
            )
        )

        // "Your Played Deck" Subtitle
        Text(
            text = "Your Played Deck",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 20.sp, // Smaller than main title
                color = BlackBoardYellow
            )
        )

        // 1. Display the horizontally scrollable deck
        DeckHorizontalScroll(
            deckCards = cardDeck.getAllCardsAsList(),
            onCardClick = onClickDeck,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Spacer for visual separation
        Spacer(modifier = Modifier.height(16.dp))

        // "Your Collection" Subtitle
        Text(
            text = "Your Collection",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = BlackBoardYellow
            )
        )

        // 2. Display the vertically scrollable collection
        collectionView(
            playerCollection = collection,
            onCardClick = onClickCollection,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Fills remaining vertical space
        )

        // Instructions for interaction
        Text(
            text = "Press the deck to remove cards",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 14.sp // Smaller for instructions,
                ,color = BlackBoardYellow
            )
        )
        Text(
            text = "Press the collection to add cards",
            modifier = Modifier
                .fillMaxWidth()
                // Apply horizontal and vertical padding first
                .padding(horizontal = 16.dp, vertical = 2.dp)
                // Then, specifically override the bottom padding
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = BlackBoardYellow
            )
        )


        // 3. Add a button at the bottom for actions like returning to the menu
        Button(
            onClick = onReturnMenu,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Return to Main Menu",
                fontFamily = Pixel // Applies Pixel font to the button text
                ,color = BlackBoardYellow
            )
        }
    }
}
