package poker

import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import poker.combinationfinder.CombinationFinder
import poker.parser.HandParser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.fromCallable
import reactor.core.publisher.ParallelFlux
import reactor.core.scheduler.Schedulers
import java.io.File
import java.io.File.createTempFile
import java.nio.file.Files.lines

@RestController
@RequestMapping("/combination")
class RestController(
    private val handParser: HandParser,
    private val combinationFinder: CombinationFinder
) {

    private val scheduler = Schedulers.newParallel("combinations-batch", 8)

    @PutMapping
    fun getHighestCombinationFrom(@RequestBody hand: Mono<String>): Mono<Pair<Combination, Hand>> =
        hand
            .map(handParser::parseHand)
            .map(combinationFinder::getHighestCombination)

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun getHighestCombinationFromBatch(@RequestPart("file") filePart: Mono<FilePart>): ParallelFlux<Pair<Combination, Hand>> {
        return Mono
            .zip(filePart, fromCallable(this::createTempFile))
            .flatMap { tuple -> tuple.t1.transferTo(tuple.t2).thenReturn(tuple.t2) }
            .flatMapMany { file ->
                Flux.using(
                    { lines(file.toPath()) },
                    { Flux.fromStream(it) },
                    { it.close() }
                )
            }
            .parallel()
            .runOn(scheduler)
            .map { line -> combinationFinder.getHighestCombination(handParser.parseHand(line)) }
    }

    private fun createTempFile(): File =
        createTempFile("combinations", ".csv")
            .apply { deleteOnExit() }

}