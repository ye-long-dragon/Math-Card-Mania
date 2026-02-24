package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.R

// This is a UI-specific data class for representing a deck in the selection screen.
// It is different from the `deck` model in the `models` package.
data class Deck(val id: String, val name: String, @DrawableRes val imageResId: Int)

@Composable
fun SelectDeckScreen(
    onDecksSelected: (Deck, Deck) -> Unit
) {
    var player1Deck by remember { mutableStateOf<Deck?>(null) }
    var player2Deck by remember { mutableStateOf<Deck?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Player 1
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlayerDeckSelection(player = "Player 1") { deck ->
                player1Deck = deck
            }
        }

        // Player 2 (rotated)
        Column(
            modifier = Modifier
                .weight(1f)
                .rotate(180f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlayerDeckSelection(player = "Player 2") { deck ->
                player2Deck = deck
            }
        }
    }

    // Both players have selected a deck
    if (player1Deck != null && player2Deck != null) {
        // Delay selection to prevent recomposition during selection
        LaunchedEffect(player1Deck, player2Deck) {
            onDecksSelected(player1Deck!!, player2Deck!!)
        }
    }
}

@Composable
fun PlayerDeckSelection(player: String, onDeckSelected: (Deck) -> Unit) {
    val sampleDecks = listOf(
        Deck("starting_deck", "Starting Deck", R.drawable.ic_launcher_foreground),
        // Add more decks as needed
    )
    var selectedDeck by remember { mutableStateOf<Deck?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "$player, choose your deck", style = MaterialTheme.typography.headlineSmall)

        // For simplicity, we'll use buttons
        sampleDecks.forEach { deck ->
            Button(
                onClick = { 
                    selectedDeck = deck
                    onDeckSelected(deck)
                },
                modifier = Modifier.padding(top = 8.dp),
                enabled = selectedDeck == null // Disable buttons after a selection is made
            ) {
                Text(deck.name)
            }
        }

        if (selectedDeck != null) {
            Text("Selected: ${selectedDeck!!.name}", modifier = Modifier.padding(top = 8.dp))
        }
    }
}