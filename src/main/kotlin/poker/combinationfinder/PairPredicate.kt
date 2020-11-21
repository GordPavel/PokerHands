package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class PairPredicate : CombinationPredicate {
    override fun getCombination() = Combination.PAIR

    override fun matches(hand: Hand) = countsByRank(hand).contains(2)
}