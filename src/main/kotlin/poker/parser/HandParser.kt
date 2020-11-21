package poker.parser

import poker.Hand

interface HandParser {
    fun parseHand(hand: String): Hand
}