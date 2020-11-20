import java.io.FileWriter
import java.nio.file.Files.lines
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.Comparator.comparing
import java.util.Comparator.comparingInt
import java.util.function.Function.identity
import java.util.stream.Collectors.toMap

fun main() {
    val combinations: Map<Hand, Pair<Combination, Hand>> = lines("poker-hands-combinations.csv".path())
        .map { line ->
            line.split(",")
                .map(String::toInt)
                .zipWithNext()
                .filterIndexed { index, _ -> index % 2 == 0 }
                .map { Card(it.first, it.second) }
        }
        .collect(toMap(
            identity(),
            { hand ->
                hand.combinations(5)
                    .map { Pair(getCombination(it), it) }
                    .maxWithOrNull(handComparator)!!
            }
        ))
    FileWriter("resolved-combinations.txt").use { file ->
        combinations.values
            .groupingBy { it.first }
            .eachCount()
            .toSortedMap(comparingInt<Combination> { it.second }.reversed())
            .forEach { (combination, count) ->
                file.write("Combination: ${combination.first}, count: $count\n")
            }
        file.write("_______________________________________________________\n")
        file.write("_______________________________________________________\n")
        combinations
            .forEach { (hand: Hand, combination: Pair<Combination, Hand>) ->
                file.write("$hand\n")
                val comb = MutableList(combination.second.size) { combination.second[it] }
                file.write(hand.joinToString(prefix = " ", postfix = " \n", separator = "  ") {
                    " ${" ".repeat(it.rank.toString().length)}${if (comb.remove(it)) "*" else " "}${" ".repeat(it.suit.toString().length)} "
                })
                file.write("${combination.first.first}\n")
                file.write("_______________________________________________________\n")
            }
    }
}

/*____________________________________________________________________________*/

val combinationComparator: Comparator<Combination> = comparingInt { it.second }

val highestCardComparator: Comparator<Hand> = comparing { getHighestCard(it) }

val handComparator: Comparator<Pair<Combination, Hand>> =
    compareBy<Pair<Combination, Hand>, Combination>(combinationComparator) { it.first }
        .thenComparing(compareBy(highestCardComparator) { it.second })

fun getCombination(hand: Hand) = when {
    isFlashRoyal(hand) -> Pair("Flash royal", 10)
    isStraightFlash(hand) -> Pair("Straight flash", 9)
    isKare(hand) -> Pair("Kare", 8)
    isFullHouse(hand) -> Pair("Full house", 7)
    isFlash(hand) -> Pair("Flash", 6)
    isStraight(hand) -> Pair("Straight", 5)
    isSet(hand) -> Pair("Set", 4)
    isTwoPairs(hand) -> Pair("Two pairs", 3)
    isPair(hand) -> Pair("Pair", 2)
    else -> Pair("High", 1)
}

fun isFlashRoyal(hand: Hand): Boolean {
    if (!isFlash(hand)) return false
    return isHighestStraight(hand)
}

fun isStraightFlash(hand: Hand): Boolean {
    if (!isFlash(hand)) return false
    return isStraight(hand)
}

fun isKare(hand: Hand) = countsByRank(hand).contains(4)

fun isFullHouse(hand: Hand): Boolean {
    val countsByRank = countsByRank(hand)
    return countsByRank.contains(3) && countsByRank.contains(2)
}

fun isFlash(hand: Hand): Boolean {
    val firstSuit = hand[0].suit
    return hand.map { it.suit }.all { it == firstSuit }
}

fun isStraight(hand: Hand) =
    isHighestStraight(hand) || hand.map { it.rank }.sorted().zipWithNext { a, b -> b - a }.all { it == 1 }

fun isSet(hand: Hand) = countsByRank(hand).contains(3)

fun isTwoPairs(hand: Hand) = countsByRank(hand).groupingBy { it }.eachCount().getOrDefault(2, 0) == 2

fun isPair(hand: Hand) = countsByRank(hand).contains(2)

/*____________________________________________________________________________*/

fun getHighestCard(hand: Hand): Card {
    return (hand.find { it.rank == 1 } ?: hand.maxByOrNull { it.rank })!!
}

fun countsByRank(hand: Hand) = hand.groupingBy { it.rank }.eachCount().values

fun isHighestStraight(hand: Hand): Boolean {
    return hand.map { it.rank }.sorted() == (listOf(1) + (10..13).toList())
}

fun String.path(): Path = Paths.get({}.javaClass.classLoader.getResource(this).toURI())

typealias Hand = List<Card>

@Suppress("unused")
fun Hand.toString(): String = this.joinToString(prefix = "[", postfix = "]")

fun <T> List<T>.combinations(m: Int): Sequence<List<T>> {
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

typealias Combination = Pair<String, Int>

data class Card(val suit: Int, val rank: Int) : Comparable<Card> {
    override fun compareTo(other: Card): Int {
        return when {
            rank == 1 -> 1
            other.rank == 1 -> -1
            else -> comparingInt<Card> { it.rank }.compare(this, other)
        }
    }

    override fun toString(): String {
        return "(${rank},${suit})"
    }
}