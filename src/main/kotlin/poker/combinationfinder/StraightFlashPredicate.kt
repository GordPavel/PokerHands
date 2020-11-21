package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class StraightFlashPredicate(
    private val straightPredicate: StraightPredicate,
    private val flashPredicate: FlashPredicate
) : CombinationPredicate {
    override fun getCombination() = Combination.STRAIGHT_FLASH

    override fun matches(hand: Hand): Boolean =
        if (!flashPredicate.matches(hand)) false else straightPredicate.matches(hand)
}