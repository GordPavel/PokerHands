import java.nio.file.Files.lines
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Comparator.comparingInt
import java.util.stream.Collectors.toList

fun String.path(): Path = Paths.get({}.javaClass.classLoader.getResource(this).toURI())

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
        .groupBy { getCombination(it) }
        .toSortedMap(comparingInt { it.second })
        .mapKeys { it.key.first }

    combinations.mapValues { it.value.size }.forEach { (combinationName, combinationsCount) ->
        println("$combinationName: count = $combinationsCount, probability = ${combinationsCount.toDouble() / hands.size}")
    }

    println("_______________________________________________________")

    val print: (String) -> Unit = {
        println("   $it")
    }
    combinations
        .mapValues { entry ->
            entry.value.map {
                it.joinToString(prefix = "[", postfix = "]") { card -> "(${card.rank},${card.suit})" }
            }
        }
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

fun getCombination(hand: List<Card>) = when {
    isFlashRoyal(hand) -> Pair("Flash royal", 1)
    isStraightFlash(hand) -> Pair("Straight flash", 2)
    isKare(hand) -> Pair("Kare", 3)
    isFullHouse(hand) -> Pair("Full house", 4)
    isFlash(hand) -> Pair("Flash", 5)
    isStraight(hand) -> Pair("Straight", 6)
    isSet(hand) -> Pair("Set", 7)
    isTwoPairs(hand) -> Pair("Two pairs", 8)
    isPair(hand) -> Pair("Pair", 9)
    else -> Pair("High", 100)
}

fun isFlashRoyal(hand: List<Card>): Boolean {
    if (!isFlash(hand)) return false
    return isHighestStraight(hand)
}

fun isStraightFlash(hand: List<Card>): Boolean {
    if (!isFlash(hand)) return false
    return isStraight(hand)
}

fun isKare(hand: List<Card>) = countsByRank(hand).contains(4)

fun isFullHouse(hand: List<Card>): Boolean {
    val countsByRank = countsByRank(hand)
    return countsByRank.contains(3) && countsByRank.contains(2)
}

fun isFlash(hand: List<Card>): Boolean {
    val firstSuit = hand[0].suit
    return hand.map { it.suit }.all { it == firstSuit }
}

fun isStraight(hand: List<Card>) =
    isHighestStraight(hand) || hand.map { it.rank }.sorted().zipWithNext { a, b -> b - a }.all { it == 1 }

fun isSet(hand: List<Card>) = countsByRank(hand).contains(3)

fun isTwoPairs(hand: List<Card>) = countsByRank(hand).groupingBy { it }.eachCount().getOrDefault(2, 0) == 2

fun isPair(hand: List<Card>) = countsByRank(hand).contains(2)

/*____________________________________________________________________________*/

fun countsByRank(hand: List<Card>) = hand.groupingBy { it.rank }.eachCount().values

fun isHighestStraight(hand: List<Card>): Boolean {
    return hand.map { it.rank }.sorted() == (listOf(1) + (10..13).toList())
}

data class Card(val suit: Int, val rank: Int)