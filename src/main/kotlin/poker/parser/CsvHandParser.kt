package poker.parser

import org.springframework.stereotype.Service
import poker.Card

@Service
class CsvHandParser : HandParser {
    override fun parseHand(hand: String): List<Card> = hand
        .split(",")
        .map(String::toInt)
        .zipWithNext()
        .filterIndexed { index, _ -> index % 2 == 0 }
        .map { Card(it.first, it.second) }
}
