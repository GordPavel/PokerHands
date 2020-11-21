package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class FlashRoyalPredicate(private val flashPredicate: FlashPredicate) : CombinationPredicate {

    override fun getCombination() = Combination.FLASH_ROYAL

    override fun matches(hand: Hand): Boolean = if (!flashPredicate.matches(hand)) false else isHighestStraight(hand)
}