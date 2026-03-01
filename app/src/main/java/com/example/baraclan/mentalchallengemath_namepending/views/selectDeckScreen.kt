package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

// ─────────────────────────────────────────────────────────────
// SelectDeckScreen
// Shows two half-screens (one rotated) for each player to pick
// a deck. Once both have picked, shows a "Ready?" confirmation.
// ─────────────────────────────────────────────────────────────
@Composable
fun SelectDeckScreen(
    availableDecks: List<deck>,           // decks loaded from local storage
    onDecksSelected: (deck, deck) -> Unit // both confirmed → start game
) {
    var player1Deck by remember { mutableStateOf<deck?>(null) }
    var player2Deck by remember { mutableStateOf<deck?>(null) }
    var showReadyScreen by remember { mutableStateOf(false) }

    if (showReadyScreen && player1Deck != null && player2Deck != null) {
        BothPlayersReadyScreen(
            p1DeckName = player1Deck!!.name,
            p2DeckName = player2Deck!!.name,
            onReady = { onDecksSelected(player1Deck!!, player2Deck!!) },
            onBack = {
                // Let players re-pick
                player1Deck = null
                player2Deck = null
                showReadyScreen = false
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Player 2 (top, rotated 180° — faces away from you) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .rotate(180f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlayerDeckSelection(
                player = "Player 2",
                availableDecks = availableDecks,
                selectedDeck = player2Deck,
                onDeckSelected = { deck ->
                    player2Deck = deck
                    if (player1Deck != null) showReadyScreen = true
                }
            )
        }

        HorizontalDivider(thickness = 2.dp, color = BlackBoardYellow)

        // ── Player 1 (bottom, normal orientation) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlayerDeckSelection(
                player = "Player 1",
                availableDecks = availableDecks,
                selectedDeck = player1Deck,
                onDeckSelected = { deck ->
                    player1Deck = deck
                    if (player2Deck != null) showReadyScreen = true
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PlayerDeckSelection — one player's half
// ─────────────────────────────────────────────────────────────
@Composable
fun PlayerDeckSelection(
    player: String,
    availableDecks: List<deck>,
    selectedDeck: deck?,
    onDeckSelected: (deck) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "$player, choose your deck",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (availableDecks.isEmpty()) {
            Text(
                text = "No decks saved yet.\nCreate a deck first!",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        } else {
            availableDecks.forEach { deck ->
                val isSelected = selectedDeck?.name == deck.name
                Button(
                    onClick = { onDeckSelected(deck) },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (isSelected) "✓ ${deck.name}" else deck.name,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) BlackBoardYellow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (selectedDeck != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected: ${selectedDeck.name}",
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BothPlayersReadyScreen — confirmation before game starts
// ─────────────────────────────────────────────────────────────
@Composable
fun BothPlayersReadyScreen(
    p1DeckName: String,
    p2DeckName: String,
    onReady: () -> Unit,
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
            text = "Both players ready?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Player 1: $p1DeckName",
            fontFamily = FontFamily.Monospace,
            color = BlackBoardYellow,
            fontSize = 16.sp
        )
        Text(
            text = "Player 2: $p2DeckName",
            fontFamily = FontFamily.Monospace,
            color = BlackBoardYellow,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onReady,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp)
        ) {
            Text(
                "START GAME",
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                "Re-select Decks",
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow
            )
        }
    }
}