package poker.combinationfinder

import org.springframework.core.Ordered
import poker.Combination
import poker.Hand

interface CombinationPredicate : Ordered {

    fun getCombination(): Combination

    fun matches(hand: Hand): Boolean

    override fun getOrder() = getCombination().importance
}