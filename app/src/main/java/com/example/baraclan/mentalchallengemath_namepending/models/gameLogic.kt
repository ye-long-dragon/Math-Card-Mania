package com.example.baraclan.mentalchallengemath_namepending.models

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