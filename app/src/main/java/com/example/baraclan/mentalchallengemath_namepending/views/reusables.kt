package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.*

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
            color = BlackBoardYellow
        )
    }
}

@Composable
public fun CardGameDisplay(
    card: cardGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
    onDragStart: (() -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null
) {
    val imageResId: Int? = remember(card) {
        when (card.type) {
            cardType.NUMBER -> when (card.numberValue) {
                0 -> R.drawable.zero
                1 -> R.drawable.one
                2 -> R.drawable.two
                3 -> R.drawable.three
                4 -> R.drawable.four
                5 -> R.drawable.five
                6 -> R.drawable.six
                7 -> R.drawable.seven
                8 -> R.drawable.eight
                9 -> R.drawable.nine
                else -> null
            }
            cardType.OPERATOR -> when (card.operator) {
                Operator.ADD -> R.drawable.addition
                Operator.SUBTRACT -> R.drawable.subtraction
                Operator.MULTIPLY -> R.drawable.multiplication
                Operator.DIVIDE -> R.drawable.division
                null -> null
            }
        }
    }

    val baseModifier = modifier
        .size(64.dp)
        .then(
            if (onLongPress != null || onDragStart != null) {
                Modifier.pointerInput(card.id) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        },
                        onLongPress = {
                            onLongPress?.invoke()
                        }
                    )
                }
            } else {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            }
        )

    if (imageResId != null) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = card.name,
            modifier = baseModifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = baseModifier
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ERR",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
public fun DeckHorizontalScroll(
    deckCards: List<cardGame>,
    onCardClick: ((cardGame) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (deckCards.isEmpty()) {
            Text(
                text = "Deck is empty.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            deckCards.forEach { card ->
                CardGameDisplay(
                    card = card,
                    onClick = { onCardClick?.invoke(card) }
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
    val cards = playerCollection.getAllCardsAsList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(
            cards,
            key = { card -> card.id }
        ) { cardGameItem ->
            CardGameDisplay(
                card = cardGameItem,
                onClick = {
                    onCardClick?.invoke(cardGameItem)
                }
            )
        }
    }
}

@Composable
public fun HandDisplay(
    playerHand: hand,
    onCardClick: ((cardGame) -> Unit)? = null,
    onCardLongPress: ((cardGame) -> Unit)? = null,
    onAreaClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardsInHand = playerHand.getAllCardsAsList()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 150.dp)
            .padding(8.dp)
            .then(if (onAreaClick != null) Modifier.clickable(onClick = onAreaClick) else Modifier),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp
    ) {
        val scrollState = rememberScrollState()
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
                    modifier = Modifier.padding(start = 8.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                cardsInHand.forEach { card ->
                    CardGameDisplay(
                        card = card,
                        onClick = { onCardClick?.invoke(card) },
                        onLongPress = onCardLongPress?.let { { it(card) } }
                    )
                }
            }
        }
    }
}

@Composable
public fun InputCardsDisplay(
    playerHand: hand,
    onCardClick: ((cardGame) -> Unit)? = null,
    onCardLongPress: ((cardGame) -> Unit)? = null,
    onAreaClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    HandDisplay(
        playerHand = playerHand,
        onCardClick = onCardClick,
        onCardLongPress = onCardLongPress,
        onAreaClick = onAreaClick,
        modifier = modifier
    )
}

@Composable
fun statusBar(score: Int, turn: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlackBoardGreen)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Score: $score",
            style = MaterialTheme.typography.titleMedium,
            color = White,
            fontFamily = Pixel
        )
        Text(
            "Turn: $turn",
            style = MaterialTheme.typography.titleMedium,
            color = White,
            fontFamily = Pixel
        )
    }
}

@Composable
fun goal(gameGoals: List<Double>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Current Goal:",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = Pixel
        )
        if (gameGoals.isNotEmpty()) {
            Text(
                gameGoals.first().toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = Pixel
            )
        } else {
            Text(
                "No Goal Set",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = Pixel
            )
        }
    }
}

@Composable
public fun EquationDisplay(
    equationElements: List<cardGame>,
    onCardClick: ((cardGame) -> Unit)? = null,
    onCardLongPress: ((cardGame) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .padding(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (equationElements.isEmpty()) {
                Text(
                    text = "Your equation will appear here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp),
                    fontFamily = Pixel
                )
            } else {
                equationElements.forEach { element ->
                    CardGameDisplay(
                        card = element,
                        onClick = { onCardClick?.invoke(element) },
                        onLongPress = onCardLongPress?.let { { it(element) } }
                    )
                }
            }
        }
    }
}