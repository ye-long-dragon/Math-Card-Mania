package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.baraclan.mentalchallengemath_namepending.models.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.items


@Composable
fun DeveloperFace(
    icon: ImageVector,
    name: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Image(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
public fun CardGameDisplay(
    card: cardGame,
    onClick: ()-> Unit, // Non-nullable click handler
    modifier: Modifier = Modifier
) {
    val cardContentText = remember(card) {
        when (card.type) {
            cardType.NUMBER -> card.numberValue.toString()
            cardType.OPERATOR -> when (card.operator) {
                Operator.ADD -> "+"
                Operator.SUBTRACT -> "-"
                Operator.MULTIPLY -> "x" // Using 'x' for multiply for visual clarity
                Operator.DIVIDE -> "÷" // Using '÷' for divide
                null -> "?" // Should not happen with validation
            }
        }
    }

    val cardBackgroundColor = when (card.type) {
        cardType.NUMBER -> MaterialTheme.colorScheme.primaryContainer
        cardType.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer
    }
    val cardContentColor = MaterialTheme.colorScheme.onBackground // Explicitly set content color

    Card(
        modifier = modifier
            .size(64.dp) // Consistent size
            .padding(4.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Enabled ripple effect as per your comment
                onClick = onClick
            ),
        shape = CardDefaults.shape, // Use default card shape
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor,
            contentColor = cardContentColor // Set contentColor for components inside the Card
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cardContentText,
                style = MaterialTheme.typography.headlineSmall,
                color = cardContentColor // Explicitly use the derived content color
            )
        }
    }
}

@Composable
public fun DeckHorizontalScroll(
    deckCards: List<cardGame>,
    onCardClick: ((cardGame) -> Unit)? = null ,// The list of cardGame objects in the deck
    modifier: Modifier = Modifier// Optional callback for when a card in the deck is clicked
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth() // Make the row take full width
            .padding(horizontal = 8.dp, vertical = 4.dp) // Padding around the entire row
            .horizontalScroll(scrollState), // Enable horizontal scrolling
        horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between cards
    ) {
        if (deckCards.isEmpty()) {
            // Display a placeholder if the deck is empty
            Text(
                text = "Deck is empty.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            deckCards.forEach { card ->
                // Display each card using the new CardGameDisplay
                CardGameDisplay(
                    card = card,
                    onClick = { onCardClick?.invoke(card) } // Pass the card object to the click handler
                )
            }
        }
    }
}


@Composable
public fun collectionView(
    playerCollection: cardContainer,
    onCardClick: ((cardGame) -> Unit)? = null,
    modifier: Modifier = Modifier
) {

        val cards = playerCollection.getAllCardsAsList() // This should return List<cardGame>

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Here's how you iterate through the 'cards' list
            items(
                cards, // Pass the list directly as the first argument
                key = { card -> card.id } // Use 'card.id' for stable IDs, as 'cardGame' has an 'id'
            ) { cardGameItem -> // 'cardGameItem' here is a single cardGame object from the list
                CardGameDisplay(
                    card = cardGameItem, // Pass the individual cardGame object to CardGameDisplay
                    onClick = {
                        onCardClick?.invoke(cardGameItem) // Invoke the callback with the clicked card
                    }
                )
            }
        }
    }

@Composable
public fun HandDisplay(
    playerHand: hand,
    onCardClick: ((cardGame) -> Unit)? = null,
    modifier: Modifier = Modifier
){
    val cardsInHand = playerHand.getAllCardsAsList() // Get all cards as a flat list

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 150.dp) // Set a height range for the hand
            .padding(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp
    ) {
        val scrollState = rememberScrollState() // For horizontal scrolling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cardsInHand.isEmpty()) {
                Text(
                    text = "Your hand is empty.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                cardsInHand.forEach { card ->
                    // Using the new CardGameDisplay
                    CardGameDisplay(card = card, onClick = { onCardClick?.invoke(card) })
                }
            }
        }
    }
}

@Composable
public fun InputCardsDisplay(
    playerHand:hand, // This is likely the player's current hand from which they select cards
    onCardClick: ((cardGame) -> Unit)? = null,
    modifier: Modifier = Modifier
){
    // Assuming InputCardsDisplay serves as the primary interactive display for the hand
    // where the user selects cards to form an equation.
    HandDisplay(
        playerHand = playerHand,
        onCardClick = onCardClick,
        modifier = modifier
    )
}




