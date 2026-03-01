package com.example.baraclan.mentalchallengemath_namepending.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.cardType
import com.example.baraclan.mentalchallengemath_namepending.models.Operator
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// Extension to get the DataStore instance from any Context
val Context.deckDataStore by preferencesDataStore(name = "saved_decks")

object DeckRepository {

    private val DECKS_KEY = stringPreferencesKey("decks_json")

    // ── READ: returns a Flow of all saved decks ───────────────
    fun getDecksFlow(context: Context): Flow<List<deck>> {
        return context.deckDataStore.data.map { prefs ->
            val json = prefs[DECKS_KEY] ?: return@map emptyList()
            deserializeDecks(json)
        }
    }

    // ── WRITE: save or overwrite a deck by name ───────────────
    suspend fun saveDeck(context: Context, deckToSave: deck) {
        context.deckDataStore.edit { prefs ->
            val existing = deserializeDecks(prefs[DECKS_KEY] ?: "[]").toMutableList()
            val index = existing.indexOfFirst { it.name == deckToSave.name }
            if (index >= 0) existing[index] = deckToSave else existing.add(deckToSave)
            prefs[DECKS_KEY] = serializeDecks(existing)
        }
    }

    // ── DELETE: remove a deck by name ─────────────────────────
    suspend fun deleteDeck(context: Context, deckName: String) {
        context.deckDataStore.edit { prefs ->
            val existing = deserializeDecks(prefs[DECKS_KEY] ?: "[]").toMutableList()
            existing.removeAll { it.name == deckName }
            prefs[DECKS_KEY] = serializeDecks(existing)
        }
    }

    // ── Serialization helpers ─────────────────────────────────

    private fun serializeDecks(decks: List<deck>): String {
        val array = JSONArray()
        decks.forEach { d ->
            val deckObj = JSONObject()
            deckObj.put("name", d.name)
            val cardsArray = JSONArray()
            d.getAllCardsWithCounts().forEach { (card, count) ->
                val cardObj = JSONObject()
                cardObj.put("cardName", card.name)      // adjust field names to match your cardGame model
                cardObj.put("count", count)
                cardsArray.put(cardObj)
            }
            deckObj.put("cards", cardsArray)
            array.put(deckObj)
        }
        return array.toString()
    }

    private fun deserializeDecks(json: String): List<deck> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val deckObj = array.getJSONObject(i)
                val name = deckObj.getString("name")
                val cardsArray = deckObj.getJSONArray("cards")
                val cardCounts = mutableMapOf<cardGame, Int>()
                for (j in 0 until cardsArray.length()) {
                    val cardObj = cardsArray.getJSONObject(j)
                    val cardName = cardObj.getString("cardName")
                    val count = cardObj.getInt("count")
                    val card = reconstructCard(cardName) ?: continue
                    cardCounts[card] = count
                }
                deck(name, cardCounts)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Rebuilds a cardGame from its stored name e.g. "Number (5)" or "Operator (+)"
    private fun reconstructCard(cardName: String): cardGame? {
        return try {
            when {
                cardName.startsWith("Number") -> {
                    val numberValue = cardName
                        .substringAfter("(")
                        .substringBefore(")")
                        .trim()
                        .toInt()
                    cardGame(
                        id = cardName, // use name as stable ID for saved cards
                        name = cardName,
                        type = cardType.NUMBER,
                        numberValue = numberValue
                    )
                }
                cardName.startsWith("Operator") -> {
                    val symbol = cardName
                        .substringAfter("(")
                        .substringBefore(")")
                        .trim()
                    val operator = when (symbol) {
                        "+" -> Operator.ADD
                        "-" -> Operator.SUBTRACT
                        "*" -> Operator.MULTIPLY
                        "/" -> Operator.DIVIDE
                        else -> return null
                    }
                    cardGame(
                        id = cardName,
                        name = cardName,
                        type = cardType.OPERATOR,
                        operator = operator
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}