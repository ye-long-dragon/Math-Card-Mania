package com.example.baraclan.mentalchallengemath_namepending.scripts

import com.example.baraclan.mentalchallengemath_namepending.models.cardContainer
import com.example.baraclan.mentalchallengemath_namepending.models.cardGame
import com.example.baraclan.mentalchallengemath_namepending.models.deck
import com.example.baraclan.mentalchallengemath_namepending.models.*
import kotlin.random.Random

fun transferCards(card: cardGame, count: Int, from: cardContainer, to: cardContainer) {
    require(count > 0) { "Count must be positive to transfer cards." }
    require(from.getCardCount(card) >= count) {
        "Cannot transfer $count x ${card.name} from ${from.name}. Only ${from.getCardCount(card)} available."
    }

    println("Transferring $count x ${card.name} from ${from.name} to ${to.name}...")
    from.removeCard(card, count)
    to.addCard(card, count)
    println("Transfer complete.")
}

fun RandomHand(Deck: deck): hand {
    // A hand needs a name
    val randomHand = hand("Player's Current Hand") // Instantiate the hand object

    // The desired capacity for the hand
    val handCapacity = 8

    // Check if the deck is empty before attempting to draw
    if (Deck.isEmpty()) {
        println("Deck is empty. Could not draw all $handCapacity cards for the hand.")
        return randomHand // Return the hand as is if deck runs out of cards
    }

    // Create a flattened list of all available cards in the deck,
    // repeating cards according to their count. This ensures weighted random selection.
    val allAvailableCardsInDeck = mutableListOf<cardGame>()
    Deck.cards.forEach { (card, count) -> // Access Deck.cards directly
        repeat(count) {
            allAvailableCardsInDeck.add(card)
        }
    }

    // Double-check if the flattened list is still not empty (e.g., all counts were 0 or negative)
    if (allAvailableCardsInDeck.isEmpty()) {
        println("No valid cards available in the deck to draw (after flattening).")
        return randomHand // Return the hand as is if no valid cards can be drawn
    }

    // Loop to draw cards until the hand is full or the deck is empty
    repeat(handCapacity) { // CHANGED: Using repeat for a fixed number of iterations


        // --- Logic to select a random card from the deck's map and remove it ---

        // Pick a random index from the weighted list
        val randomIndex = Random.nextInt(allAvailableCardsInDeck.size)
        val drawnCard = allAvailableCardsInDeck[randomIndex]

        // Add the drawn card to the new hand
        randomHand.addCard(drawnCard, 1)

        // Remove one instance of the drawn card from the original Deck's map
        // (This modifies the Deck's state)
        val currentCountInDeck = Deck.cards[drawnCard] ?: 0
        if (currentCountInDeck > 1) {
            Deck.cards[drawnCard] = currentCountInDeck - 1
        } else {
            // If count is 1, remove the card entry entirely from the map
            Deck.cards.remove(drawnCard)
        }
        // --- End of draw and remove logic ---

        println("Drawn card ${drawnCard.name} for hand. Hand size: ${randomHand.getTotalCount()}")
    }

    return randomHand
}

// Placeholder for equation evaluation logic (kept for backward compatibility)
fun evaluateEquation(equation: List<cardGame>): Double {
    if (equation.isEmpty()) return 0.0
    try {
        return PemdasEvaluator.evaluate(equation).toDouble()
    } catch (e: Exception) {
        return 0.0
    }
}

// PEMDAS evaluation function using the PemdasEvaluator
fun evaluateEquationWithPemdas(equation: List<cardGame>): Int {
    require(equation.isNotEmpty()) { "Equation cannot be empty." }
    return PemdasEvaluator.evaluate(equation)
}

// Get equation as a display string
fun getEquationString(equation: List<cardGame>): String {
    return equation.joinToString(" ") {
        when (it.type) {
            cardType.NUMBER -> it.numberValue.toString()
            cardType.OPERATOR -> when (it.operator) {
                Operator.ADD -> "+"
                Operator.SUBTRACT -> "-"
                Operator.MULTIPLY -> "×"
                Operator.DIVIDE -> "÷"
                null -> "?"
            }
        }
    }
}


