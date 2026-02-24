package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.baraclan.mentalchallengemath_namepending.R
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckSelectScreen(
    onDeckSelected: (Deck) -> Unit = {}
) {
    var decks by remember {
        mutableStateOf(
            listOf(
                Deck("starting_deck", "Starting Deck", R.drawable.ic_launcher_foreground),
            )
        )
    }
    var inSelectionMode by remember { mutableStateOf(false) }
    var selectedDecks by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose your Deck") },
                actions = {
                    if (inSelectionMode) {
                        IconButton(onClick = {
                            decks = decks.filter { it.id !in selectedDecks }
                            inSelectionMode = false
                            selectedDecks = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected decks")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Implement add new deck */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add new deck")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(decks, key = { it.id }) { deck ->
                    val isSelected = deck.id in selectedDecks
                    DeckTile(
                        deck = deck,
                        isSelected = isSelected,
                        inSelectionMode = inSelectionMode,
                        onClick = {
                            if (inSelectionMode) {
                                selectedDecks = if (isSelected) {
                                    selectedDecks - deck.id
                                } else {
                                    selectedDecks + deck.id
                                }
                            } else {
                                onDeckSelected(deck)
                            }
                        },
                        onLongClick = {
                            if (!inSelectionMode) {
                                inSelectionMode = true
                                selectedDecks = selectedDecks + deck.id
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckTile(
    deck: Deck,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(140.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else BlackBoardYellow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = deck.imageResId),
                    contentDescription = "Deck image for ${deck.name}",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
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
