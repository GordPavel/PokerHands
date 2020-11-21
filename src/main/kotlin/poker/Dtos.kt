package poker

import java.util.*

data class Card(val suit: Int, val rank: Int) : Comparable<Card> {
    override fun compareTo(other: Card): Int {
        return when {
            rank == 1 -> 1
            other.rank == 1 -> -1
            else -> Comparator.comparingInt<Card> { it.rank }.compare(this, other)
        }
    }
}

enum class Combination(val combinationName: String, val importance: Int) {
    FLASH_ROYAL("Flash royal", 1),
    STRAIGHT_FLASH("Straight flash", 2),
    KARE("Kare", 3),
    FULL_HOUSE("Full house", 4),
    FLASH("Flash", 5),
    STRAIGHT("Straight", 6),
    SET("Set", 7),
    TWO_PAIRS("Two pairs", 8),
    PAIR("Pair", 9),
    HIGH("High", 10),
    /**/;
}

typealias Hand = List<Card>