package poker

import org.springframework.beans.factory.annotation.Qualifier
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
import reactor.core.scheduler.Scheduler
import java.io.File
import java.io.File.createTempFile
import java.nio.file.Files.lines

@RestController
@RequestMapping("/combination")
class RestController(
    private val handParser: HandParser,
    private val combinationFinder: CombinationFinder,
    @Qualifier("controllerScheduler")
    private val scheduler: Scheduler
) {

    @PutMapping
    fun getHighestCombinationFrom(@RequestBody hand: Mono<String>): Mono<Result> =
        hand
            .map(handParser::parseHand)
            .map(combinationFinder::getHighestCombination)

    @PatchMapping(produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun getHighestCombinationFrom(@RequestBody hands: List<String>): ParallelFlux<Result> {
        return Flux.fromIterable(hands)
            .parallel()
            .runOn(scheduler)
            .map { line -> combinationFinder.getHighestCombination(handParser.parseHand(line)) }
    }

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun getHighestCombinationFromBatch(@RequestPart("file") filePart: Mono<FilePart>): ParallelFlux<Result> =
        Mono
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

    private fun createTempFile(): File =
        createTempFile("combinations", ".csv")
            .apply { deleteOnExit() }

}