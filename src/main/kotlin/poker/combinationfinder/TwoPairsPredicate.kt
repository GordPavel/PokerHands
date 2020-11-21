package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class TwoPairsPredicate : CombinationPredicate {
    override fun getCombination() = Combination.TWO_PAIRS

    override fun matches(hand: Hand) = countsByRank(hand).groupingBy { it }.eachCount().getOrDefault(2, 0) == 2
}