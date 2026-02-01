package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.views.*


@Composable
public fun EditDeckScreen(
    cardDeck: deck, // Assuming 'deck' is a class with a 'cards' property: List<cardGame>
    collection: collection, // Assuming 'collection' is a class with a 'cards' property: List<cardGame>
    onReturnMenu: () -> Unit = {},
    onClickDeck: (cardGame) -> Unit = {}, // Callback for clicking a card in the deck
    onClickCollection: (cardGame) -> Unit = {}, // Callback for clicking a card in the collection
){
    Column(
        modifier = Modifier.fillMaxSize()// This Column takes up the whole screen
    ) {
        // 1. Display the horizontally scrollable deck
        DeckHorizontalScroll(
            // Pass the actual list of cards from the 'deck' object
            deckCards = cardDeck.getAllCardsAsList(), // MODIFIED HERE
            onCardClick = onClickDeck,
            modifier = Modifier
                .fillMaxWidth() // Make the deck row span the full width
                .padding(bottom = 8.dp) // Add some padding below the deck
        )

        // 2. Display the vertically scrollable collection
        collectionView(
            // Pass the actual list of cards from the 'collection' object
            playerCollection = collection, // Pass the 'collection' object which is of type cardContainer
            // The function's internal 'cards' variable will then call getAllCardsAsList()
            onCardClick = onClickCollection,
            // Crucially, use .weight(1f) to make this view fill the remaining vertical space
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )


        // 3. Add a button at the bottom for actions like returning to the menu
        Button(
            onClick = onReturnMenu,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Return to Main Menu",
                fontFamily = Pixel)
        }
    }
}
