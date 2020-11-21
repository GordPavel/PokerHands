package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class StraightPredicate : CombinationPredicate {
    override fun getCombination() = Combination.STRAIGHT

    override fun matches(hand: Hand) =
        isHighestStraight(hand) || hand.map { it.rank }.sorted().zipWithNext { a, b -> b - a }.all { it == 1 }
}