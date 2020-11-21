package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class FlashPredicate : CombinationPredicate {
    override fun getCombination() = Combination.FLASH

    override fun matches(hand: Hand): Boolean {
        val firstSuit = hand.first().suit
        return hand.map { it.suit }.all { it == firstSuit }
    }
}