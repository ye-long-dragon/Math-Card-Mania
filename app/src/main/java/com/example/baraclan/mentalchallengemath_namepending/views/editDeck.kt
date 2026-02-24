package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.collection
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

@Composable
fun EditDeckScreen(
    cardDeck: deck,
    collection: collection,
    onReturnMenu: () -> Unit = {},
    onDeckCardClick: (cardGame) -> Unit = {},
    onCollectionCardClick: (cardGame) -> Unit = {},
) {
    val cardCount = cardDeck.getTotalCount()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Edit Deck",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                color = BlackBoardYellow
            )
        )

        Text(
            text = "Your Played Deck ($cardCount/20)",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = if (cardCount > 20) MaterialTheme.colorScheme.error else BlackBoardYellow
            )
        )

        DeckHorizontalScroll(
            deckCards = cardDeck.getAllCardsAsList(),
            onCardClick = onDeckCardClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Collection",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
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

        Text(
            text = "Press the deck to remove cards",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = BlackBoardYellow
            )
        )
        Text(
            text = "Press the collection to add cards",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = BlackBoardYellow
            )
        )


        Button(
            onClick = onReturnMenu,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Return to Main Menu",
                fontFamily = FontFamily.Monospace,
                color = BlackBoardYellow
            )
        }
    }
}
