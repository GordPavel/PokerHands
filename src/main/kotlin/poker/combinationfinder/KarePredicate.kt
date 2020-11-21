package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class KarePredicate : CombinationPredicate {
    override fun getCombination() = Combination.KARE

    override fun matches(hand: Hand) = countsByRank(hand).contains(4)
}