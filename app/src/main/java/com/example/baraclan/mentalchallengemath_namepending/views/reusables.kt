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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.baraclan.mentalchallengemath_namepending.models.*

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
public fun CardGameDisplay( // This is the new/modified card view for cardGame objects
    card: cardGame,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null // Optional click handler for interactivity
) {
    // Derive the string content from the cardGame object
    val cardContentText = remember(card) {
        when (card.type) {
            cardType.NUMBER -> card.numberValue.toString()
            cardType.OPERATOR -> when (card.operator) {
                Operator.ADD -> "+"
                Operator.SUBTRACT -> "-"
                Operator.MULTIPLY -> "*"
                Operator.DIVIDE -> "/"
                null -> "?" // Should not happen with validation
            }
        }
    }

    Card(
        modifier = modifier
            .size(64.dp) // Consistent size with your original cardView
            .padding(4.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // Using ripple as in your original `cardView` example
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = CardDefaults.shape, // Use default card shape
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (card.type) {
                cardType.NUMBER -> MaterialTheme.colorScheme.primaryContainer
                cardType.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = MaterialTheme.colorScheme.onBackground // Use onBackground for text for better contrast
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cardContentText,
                style = MaterialTheme.typography.headlineSmall,
                color = LocalContentColor.current // Use local content color or explicitly set
            )
        }
    }
}

@Composable
public fun DeckHorizontalScroll(
    deckCards: List<cardGame>, // The list of cardGame objects in the deck
    modifier: Modifier = Modifier,
    onCardClick: ((cardGame) -> Unit)? = null // Optional callback for when a card in the deck is clicked
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



