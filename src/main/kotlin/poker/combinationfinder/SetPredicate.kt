package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class SetPredicate : CombinationPredicate {
    override fun getCombination() = Combination.SET

    override fun matches(hand: Hand) = countsByRank(hand).contains(3)
}