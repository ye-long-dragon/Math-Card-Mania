package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.baraclan.mentalchallengemath_namepending.models.*
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

// ─────────────────────────────────────────────────────────────
// GameTopBar — back + show deck + settings
// ─────────────────────────────────────────────────────────────
@Composable
fun GameTopBar(
    onReturnToMenu: () -> Unit,
    onShowDeck: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onReturnToMenu) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Return to Menu", tint = BlackBoardYellow)
        }
        Row {
            IconButton(onClick = onShowDeck) {
                Icon(Icons.Default.Style, contentDescription = "Show Deck", tint = BlackBoardYellow)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = BlackBoardYellow)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ShowDeckOverlay — shows all cards with visual states:
//   cardBin cards  → 50% opacity
//   hand cards     → yellow tint/glow border
//   deck cards     → normal
// ─────────────────────────────────────────────────────────────
@Composable
fun ShowDeckOverlay(
    allDeckCards: List<cardGame>,      // full original deck
    handCards: List<cardGame>,         // currently in hand
    binCards: List<cardGame>,          // discarded / submitted
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Deck", fontFamily = Pixel, color = BlackBoardYellow, fontSize = 20.sp)
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = BlackBoardYellow, fontSize = 18.sp)
                    }
                }

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(color = BlackBoardYellow, label = "In deck")
                    LegendItem(color = BlackBoardYellow, label = "In hand", isGlowing = true)
                    LegendItem(color = BlackBoardYellow.copy(alpha = 0.4f), label = "Used")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val binIds = binCards.map { it.id }.toSet()
                val handIds = handCards.map { it.id }.toSet()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(allDeckCards, key = { index, card -> "${index}_${card.id}" }) { _, card ->
                        val isInBin = card.id in binIds
                        val isInHand = card.id in handIds

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .then(
                                    if (isInHand) Modifier
                                        .border(2.dp, BlackBoardYellow, RoundedCornerShape(6.dp))
                                        .background(
                                            BlackBoardYellow.copy(alpha = 0.15f),
                                            RoundedCornerShape(6.dp)
                                        )
                                    else Modifier
                                )
                        ) {
                            Box(modifier = Modifier.alpha(if (isInBin) 0.35f else 1f)) {
                                CardGameDisplay(
                                    card = card,
                                    onClick = {}  // read-only in overlay
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, isGlowing: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(if (isGlowing) Modifier.border(1.dp, BlackBoardYellow, RoundedCornerShape(3.dp)) else Modifier)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontFamily = Pixel, color = BlackBoardYellow, fontSize = 10.sp)
    }
}