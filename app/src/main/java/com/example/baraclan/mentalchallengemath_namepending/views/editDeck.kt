package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.data.DeckRepository
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.collection
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch

@Composable
fun EditDeckScreen(
    cardDeck: deck,
    collection: collection,
    onReturnMenu: () -> Unit = {},
    onDeckCardClick: (cardGame) -> Unit = {},
    onCollectionCardClick: (cardGame) -> Unit = {}
) {
    val cardCount = cardDeck.getTotalCount()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var saveStatus by remember { mutableStateOf<String?>(null) }

    // ── Deck validation rules ─────────────────────────────────
    // Max 30 cards, min 5 operators, min 1 of every variable letter
    val allCards = cardDeck.getAllCardsAsList()
    val isOverLimit  = cardCount > 30
    val opCount      = allCards.count { it.type == cardType.OPERATOR }
    val hasEnoughOps = opCount >= 5
    val varOps = listOf(Operator.VAR_A, Operator.VAR_B, Operator.VAR_C, Operator.VAR_D,
        Operator.VAR_X, Operator.VAR_Y, Operator.VAR_Z)
    val missingVars  = varOps.filter { op -> allCards.none { it.type == cardType.VARIABLE && it.operator == op } }
    val deckValid    = !isOverLimit && hasEnoughOps && missingVars.isEmpty()

    val validationMsg: String? = when {
        isOverLimit      -> "Too many cards! Max 30 (currently $cardCount)"
        !hasEnoughOps    -> "Need at least 5 operator cards (have $opCount)"
        missingVars.isNotEmpty() -> "Missing variables: ${missingVars.joinToString { it.name.removePrefix("VAR_").lowercase() }}"
        else             -> null
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Title ─────────────────────────────────────────────
        Text(
            text = "Edit Deck",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                color = BlackBoardYellow
            )
        )

        // ── Deck count (red if over limit) ────────────────────
        Text(
            text = "Your Played Deck ($cardCount/30)" + if (validationMsg != null) " ⚠" else " ✓",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = if (validationMsg != null) MaterialTheme.colorScheme.error else BlackBoardYellow
            )
        )

        // ── Validation hint ───────────────────────────────────
        if (validationMsg != null) {
            Text(
                text = validationMsg!!,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = Pixel,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            )
        }

        // ── Deck horizontal scroll ────────────────────────────
        DeckHorizontalScroll(
            deckCards = cardDeck.getAllCardsAsList(),
            onCardClick = onDeckCardClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Collection ────────────────────────────────────────
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

        collectionView(
            playerCollection = collection,
            onCardClick = onCollectionCardClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // ── Hints ─────────────────────────────────────────────
        Text(
            text = "Tap deck cards to remove  •  Tap collection cards to add",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = BlackBoardYellow.copy(alpha = 0.7f)
            )
        )

        // ── Save status message ───────────────────────────────
        saveStatus?.let {
            Text(
                text = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = if (it.startsWith("✓")) BlackBoardYellow else MaterialTheme.colorScheme.error
            )
        }

        // ── Buttons ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Save deck button
            Button(
                onClick = {
                    if (validationMsg != null) {
                        saveStatus = validationMsg
                        return@Button
                    }
                    scope.launch {
                        try {
                            DeckRepository.saveDeck(context, cardDeck)
                            saveStatus = "✓ Deck saved!"
                        } catch (e: Exception) {
                            saveStatus = "Save failed: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = deckValid
            ) {
                Text(
                    "Save Deck",
                    fontFamily = Pixel,
                    color = BlackBoardYellow
                )
            }

            // Return button
            OutlinedButton(
                onClick = onReturnMenu,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Return to Menu",
                    fontFamily = Pixel,
                    color = BlackBoardYellow
                )
            }
        }
    }
}