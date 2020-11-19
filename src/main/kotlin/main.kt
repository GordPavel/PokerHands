import java.nio.file.Files.lines
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.Comparator.comparing
import java.util.Comparator.comparingInt
import java.util.stream.Collectors.toList

fun main() {
    val hands = lines("poker-hands.csv".path())
        .map { line ->
            line.split(",")
                .map(String::toInt)
                .zipWithNext()
                .filterIndexed { index, _ -> index % 2 == 0 }
                .map { Card(it.first, it.second) }
        }
        .collect(toList())
    val combinations = hands
        .map { Pair(getCombination(it), it) }
        .sortedWith(
            compareBy<Pair<Combination, Hand>, Combination>(combinationComparator) { it.first }
                .thenComparing(compareBy(highestCardComparator) { it.second })
                .reversed()
        )
        .groupBy { it.first }
        .toSortedMap(combinationComparator.reversed())
        .mapKeys { it.key.first }
        .mapValues { pair -> pair.value.map { it.second } }

    combinations.mapValues { it.value.size }.forEach { (combinationName, combinationsCount) ->
        println("$combinationName: count = $combinationsCount, probability = ${combinationsCount.toDouble() / hands.size}")
    }

    println("_______________________________________________________")

    val print: (String) -> Unit = {
        println("   $it")
    }
    combinations
        .mapValues { entry -> entry.value.map { it.joinToString(prefix = "[", postfix = "]") } }
        .forEach { (combinationName, hands) ->
            println(combinationName)
            if (hands.size <= 5) {
                hands.forEach(print)
            } else {
                hands.slice(0..4).forEach(print)
                println("   ...")
            }
            println("_______________________________________________________")
        }
}

/*____________________________________________________________________________*/

val combinationComparator: Comparator<Combination> = comparingInt { it.second }

val highestCardComparator: Comparator<Hand> = comparing { getHighestCard(it) }

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