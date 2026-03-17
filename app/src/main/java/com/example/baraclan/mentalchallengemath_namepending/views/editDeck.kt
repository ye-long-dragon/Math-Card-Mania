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

    // ── Deck validation — safe range 20-30 cards ─────────────
    val allCards     = cardDeck.getAllCardsAsList()
    val isTooMany    = cardCount > 30
    val isTooFew     = cardCount < 20
    // OPERATOR = +/-/×/÷ only.  FUNCTION = sin/cos/ln/log10 only.
    // VARIABLE (a-z) is its own type — never counted as a function.
    val opCount      = allCards.count { it.type == cardType.OPERATOR }
    val fnCount      = allCards.count { it.type == cardType.FUNCTION }
    val varCount     = allCards.count { it.type == cardType.VARIABLE }
    val hasEnoughOps = opCount >= 5
    val hasEnoughFns = fnCount >= 5
    val varOps = listOf(Operator.VAR_A, Operator.VAR_B, Operator.VAR_C, Operator.VAR_D,
        Operator.VAR_X, Operator.VAR_Y, Operator.VAR_Z)
    val missingVars  = varOps.filter { op -> allCards.none { it.type == cardType.VARIABLE && it.operator == op } }
    val deckValid    = !isTooMany && !isTooFew && hasEnoughOps && hasEnoughFns && missingVars.isEmpty()

    val validationIssues = buildList {
        if (isTooMany)                add("Too many cards — max 30 (have $cardCount)")
        if (isTooFew)                 add("Too few cards — min 20 (have $cardCount)")
        if (!hasEnoughOps)            add("Need ≥5 operators (+−×÷) (have $opCount)")
        if (!hasEnoughFns)            add("Need ≥5 functions (sin/cos/ln/log) (have $fnCount)")
        if (missingVars.isNotEmpty()) add("Missing variables: ${missingVars.joinToString { it.name.removePrefix("VAR_").lowercase() }}")
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
            text = "Your Deck  $cardCount cards" + if (deckValid) "  ✓" else "  ⚠ ${validationIssues.size} issue(s)",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Pixel,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = if (deckValid) BlackBoardYellow else MaterialTheme.colorScheme.error
            )
        )

        // ── Validation hints ──────────────────────────────────
        if (validationIssues.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                validationIssues.forEach { issue ->
                    Text(
                        text = "• $issue",
                        fontFamily = Pixel,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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
                    if (!deckValid) {
                        saveStatus = validationIssues.first()
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