package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Combination
import poker.Hand

@Service
internal class FullHousePredicate : CombinationPredicate {
    override fun getCombination() = Combination.FULL_HOUSE

    override fun matches(hand: Hand): Boolean {
        val countsByRank = countsByRank(hand)
        return countsByRank.contains(3) && countsByRank.contains(2)
    }
}