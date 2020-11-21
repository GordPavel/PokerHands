package poker.parser

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import poker.Card

@Service
class CsvHandParser(
    @Value("\${csv.delimiter:,}")
    private val csvDelimiter: String
) : HandParser {

    override fun parseHand(hand: String): List<Card> {
        return hand
            .split(csvDelimiter)
            .map(String::toInt)
            .zipWithNext()
            .filterIndexed { index, _ -> index % 2 == 0 }
            .map { Card(it.first, it.second) }
    }
}
