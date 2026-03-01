package com.example.baraclan.mentalchallengemath_namepending.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.baraclan.mentalchallengemath_namepending.data.DeckRepository
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.collection
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.BlackBoardYellow
import kotlinx.coroutines.launch

const val MAX_DECKS = 8

// ─────────────────────────────────────────────────────────────
// DeckManagerScreen
// Shows all saved decks. Tap to edit, long-press to select,
// FAB to add. Supports up to MAX_DECKS decks.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckManagerScreen(
    defaultDeck: deck,                   // the initDeck() starter deck
    availableCollection: collection,
    onReturnMenu: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedDecks by DeckRepository.getDecksFlow(context).collectAsState(initial = emptyList())

    var selectedDeckName by remember { mutableStateOf<String?>(null) }  // deck being edited
    var inSelectionMode by remember { mutableStateOf(false) }
    var selectedForDelete by remember { mutableStateOf(emptySet<String>()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // If a deck is tapped, go straight to edit view
    if (selectedDeckName != null) {
        val deckToEdit = savedDecks.find { it.name == selectedDeckName }
        if (deckToEdit != null) {
            EditDeckScreen(
                cardDeck = deckToEdit,
                collection = availableCollection,
                onReturnMenu = { selectedDeckName = null },  // go back to manager
                onDeckCardClick = { cardToRemove ->
                    scope.launch {
                        val updated = deck(deckToEdit.name, deckToEdit.getAllCardsWithCounts())
                        updated.removeCard(cardToRemove, 1)
                        DeckRepository.saveDeck(context, updated)
                    }
                },
                onCollectionCardClick = { cardToAdd ->
                    scope.launch {
                        val updated = deck(deckToEdit.name, deckToEdit.getAllCardsWithCounts())
                        updated.addCard(cardToAdd, 1)
                        DeckRepository.saveDeck(context, updated)
                    }
                }
            )
            return
        }
    }

    // ── Add Deck Dialog ───────────────────────────────────────
    if (showAddDialog) {
        AddDeckDialog(
            existingDecks = savedDecks,
            defaultDeck = defaultDeck,
            onDismiss = { showAddDialog = false },
            onCreateNew = { name ->
                scope.launch {
                    DeckRepository.saveDeck(context, deck(name))
                    showAddDialog = false
                }
            },
            onDuplicate = { sourceDeck, newName ->
                scope.launch {
                    val copy = deck(newName, sourceDeck.getAllCardsWithCounts())
                    DeckRepository.saveDeck(context, copy)
                    showAddDialog = false
                }
            },
            onResetDefault = { name ->
                scope.launch {
                    val reset = deck(name, defaultDeck.getAllCardsWithCounts())
                    DeckRepository.saveDeck(context, reset)
                    showAddDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Decks",
                        fontFamily = FontFamily.Monospace,
                        color = BlackBoardYellow
                    )
                },
                actions = {
                    if (inSelectionMode && selectedForDelete.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch {
                                selectedForDelete.forEach { DeckRepository.deleteDeck(context, it) }
                            }
                            inSelectionMode = false
                            selectedForDelete = emptySet()
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (inSelectionMode) {
                        TextButton(onClick = {
                            inSelectionMode = false
                            selectedForDelete = emptySet()
                        }) {
                            Text("Cancel", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!inSelectionMode && savedDecks.size < MAX_DECKS) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add deck")
                }
            }
        },
        bottomBar = {
            Button(
                onClick = onReturnMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Return to Menu", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Deck count indicator
            Text(
                text = "${savedDecks.size} / $MAX_DECKS decks" +
                        if (savedDecks.size >= MAX_DECKS) " (limit reached)" else "",
                fontFamily = FontFamily.Monospace,
                color = if (savedDecks.size >= MAX_DECKS)
                    MaterialTheme.colorScheme.error
                else
                    BlackBoardYellow.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            if (inSelectionMode) {
                Text(
                    text = "Long-press to select • Tap 🗑 to delete selected",
                    fontFamily = FontFamily.Monospace,
                    color = BlackBoardYellow.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            if (savedDecks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No decks yet!",
                            fontFamily = FontFamily.Monospace,
                            color = BlackBoardYellow,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to create your first deck",
                            fontFamily = FontFamily.Monospace,
                            color = BlackBoardYellow.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(savedDecks, key = { it.name }) { d ->
                        val isSelected = d.name in selectedForDelete
                        DeckManagerTile(
                            deckName = d.name,
                            cardCount = d.getTotalCount(),
                            isSelected = isSelected,
                            inSelectionMode = inSelectionMode,
                            onClick = {
                                if (inSelectionMode) {
                                    selectedForDelete = if (isSelected)
                                        selectedForDelete - d.name
                                    else
                                        selectedForDelete + d.name
                                } else {
                                    selectedDeckName = d.name
                                }
                            },
                            onLongClick = {
                                inSelectionMode = true
                                selectedForDelete = selectedForDelete + d.name
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DeckManagerTile
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckManagerTile(
    deckName: String,
    cardCount: Int,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                BlackBoardYellow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = deckName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "$cardCount / 20 cards",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = if (cardCount > 20) MaterialTheme.colorScheme.error else Color.DarkGray
                    )
                }
                if (!inSelectionMode) {
                    Text(
                        text = "Tap to edit →",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AddDeckDialog — create new / duplicate / reset to default
// ─────────────────────────────────────────────────────────────
@Composable
fun AddDeckDialog(
    existingDecks: List<deck>,
    defaultDeck: deck,
    onDismiss: () -> Unit,
    onCreateNew: (name: String) -> Unit,
    onDuplicate: (source: deck, newName: String) -> Unit,
    onResetDefault: (name: String) -> Unit
) {
    // Tab state: 0 = New, 1 = Duplicate, 2 = Reset
    var selectedTab by remember { mutableStateOf(0) }
    var deckName by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf<deck?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    fun validateName(name: String): Boolean {
        return when {
            name.isBlank() -> { nameError = "Name cannot be empty"; false }
            existingDecks.any { it.name == name.trim() } -> { nameError = "A deck with this name already exists"; false }
            else -> { nameError = null; true }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Deck",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    color = BlackBoardYellow,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("New", "Duplicate", "Reset").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                deckName = ""
                                nameError = null
                            },
                            text = {
                                Text(title, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    // ── New empty deck ────────────────────────
                    0 -> {
                        OutlinedTextField(
                            value = deckName,
                            onValueChange = { deckName = it; nameError = null },
                            label = { Text("Deck name", fontFamily = FontFamily.Monospace) },
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { if (validateName(deckName)) onCreateNew(deckName.trim()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Empty Deck", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                        }
                    }

                    // ── Duplicate existing deck ───────────────
                    1 -> {
                        if (existingDecks.isEmpty()) {
                            Text(
                                "No decks to duplicate yet.",
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                "Choose a deck to copy:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = BlackBoardYellow,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            existingDecks.forEach { d ->
                                OutlinedButton(
                                    onClick = {
                                        selectedSource = d
                                        deckName = "${d.name} (copy)"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.let {
                                        if (selectedSource?.name == d.name)
                                            ButtonDefaults.outlinedButtonBorder
                                        else it
                                    }
                                ) {
                                    Text(
                                        "${d.name} (${d.getTotalCount()} cards)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = if (selectedSource?.name == d.name)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            BlackBoardYellow
                                    )
                                }
                            }
                            if (selectedSource != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = deckName,
                                    onValueChange = { deckName = it; nameError = null },
                                    label = { Text("New deck name", fontFamily = FontFamily.Monospace) },
                                    isError = nameError != null,
                                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (validateName(deckName))
                                            onDuplicate(selectedSource!!, deckName.trim())
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Duplicate", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                                }
                            }
                        }
                    }

                    // ── Reset to default starter deck ─────────
                    2 -> {
                        Text(
                            text = "Creates a new deck pre-filled with the default starting cards (${defaultDeck.getTotalCount()} cards).",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = BlackBoardYellow,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = deckName,
                            onValueChange = { deckName = it; nameError = null },
                            label = { Text("Deck name", fontFamily = FontFamily.Monospace) },
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { if (validateName(deckName)) onResetDefault(deckName.trim()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create from Default", fontFamily = FontFamily.Monospace, color = BlackBoardYellow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}