package poker.combinationfinder

import org.springframework.stereotype.Service
import poker.Card
import poker.Combination
import poker.Combination.HIGH
import poker.Hand
import poker.Result
import java.util.*
import java.util.Comparator.comparing
import java.util.Comparator.comparingInt

val combinationComparator: Comparator<Combination> = comparingInt<Combination> { it.importance }.reversed()

val highestCardComparator: Comparator<Hand> = comparing { getHighestCard(it) }

val handComparator: Comparator<Result> =
    compareBy<Result, Combination>(combinationComparator) { it.combination }
        .thenComparing(compareBy(highestCardComparator) { it.hand })

@Service
open class CombinationFinder(private val combinationPredicates: List<CombinationPredicate>) {
    open fun getHighestCombination(hand: Hand): Result {
        require(hand.size >= 5) { "Input hand size should be at least 5: $hand" }
        return hand
            .combinations(5)
            .map { Result(getCombination(it), it) }
            .maxWithOrNull(handComparator)!!
    }

    private fun getCombination(hand: Hand): Combination = combinationPredicates
        .stream()
        .filter { it.matches(hand) }
        .findFirst()
        .map(CombinationPredicate::getCombination)
        .orElse(HIGH)

    private fun <T> List<T>.combinations(m: Int): Sequence<List<T>> {
        val list = this
        return sequence {
            val n = list.size
            val result = MutableList(m) { list[0] }
            val stack = LinkedList<Int>()
            stack.push(0)
            while (stack.isNotEmpty()) {
                var resIndex = stack.size - 1
                var arrIndex = stack.pop()

                while (arrIndex < n) {
                    result[resIndex++] = list[arrIndex++]
                    stack.push(arrIndex)

                    if (resIndex == m) {
                        yield(result.toList())
                        break
                    }
                }
            }
        }
    }

}

internal fun isHighestStraight(hand: Hand): Boolean {
    return hand.map { it.rank }.sorted() == (listOf(1) + (10..13).toList())
}

internal fun countsByRank(hand: Hand) = hand.groupingBy { it.rank }.eachCount().values

internal fun getHighestCard(hand: Hand): Card {
    return (hand.find { it.rank == 1 } ?: hand.maxByOrNull { it.rank })!!
}