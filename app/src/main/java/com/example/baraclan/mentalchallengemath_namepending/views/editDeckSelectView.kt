package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckSelectScreen(
    onDeckSelected: (deck) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load decks from local storage
    val savedDecks by DeckRepository.getDecksFlow(context).collectAsState(initial = emptyList())

    var inSelectionMode by remember { mutableStateOf(false) }
    var selectedDeckNames by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Choose your Deck",
                        fontFamily = FontFamily.Monospace,
                        color = BlackBoardYellow
                    )
                },
                actions = {
                    if (inSelectionMode) {
                        IconButton(onClick = {
                            scope.launch {
                                selectedDeckNames.forEach { name ->
                                    DeckRepository.deleteDeck(context, name)
                                }
                            }
                            inSelectionMode = false
                            selectedDeckNames = emptySet()
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected decks",
                                tint = BlackBoardYellow
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (savedDecks.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No saved decks yet.",
                            fontFamily = FontFamily.Monospace,
                            color = BlackBoardYellow,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Build a deck from the Edit Deck screen first!",
                            fontFamily = FontFamily.Monospace,
                            color = BlackBoardYellow.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (inSelectionMode) {
                    Text(
                        text = "Long-press to select • Tap trash to delete",
                        fontFamily = FontFamily.Monospace,
                        color = BlackBoardYellow.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedDecks, key = { it.name }) { deck ->
                        val isSelected = deck.name in selectedDeckNames
                        DeckTile(
                            deck = deck,
                            isSelected = isSelected,
                            inSelectionMode = inSelectionMode,
                            onClick = {
                                if (inSelectionMode) {
                                    selectedDeckNames = if (isSelected)
                                        selectedDeckNames - deck.name
                                    else
                                        selectedDeckNames + deck.name
                                } else {
                                    onDeckSelected(deck)
                                }
                            },
                            onLongClick = {
                                if (!inSelectionMode) {
                                    inSelectionMode = true
                                    selectedDeckNames = selectedDeckNames + deck.name
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckTile(
    deck: deck,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(80.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                BlackBoardYellow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.surface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${deck.getTotalCount()} cards",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}