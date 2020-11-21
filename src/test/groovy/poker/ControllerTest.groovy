package poker

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.web.reactive.server.WebTestClient
import poker.combinationfinder.CombinationFinder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.ThreadLocalRandom

import static java.util.function.Predicate.isEqual
import static org.assertj.core.api.Assertions.assertThat
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty
import static org.hamcrest.core.AllOf.allOf
import static org.hamcrest.core.IsEqual.equalTo
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ControllerTest extends Specification {

    @SpringBean
    CombinationFinder combinationFinder = Mock() {
        _ * getHighestCombination(_) >> { arguments -> new Result(Combination.HIGH, arguments[0] as List<Card>) }
    }

    @Autowired
    WebTestClient webClient

    @Value("classpath:poker-hands-combinations.csv")
    Resource testFileResource

    @Unroll
    def "test put endpoint with #handString hand"() {
        expect:
        webClient.put()
                .uri("/combination")
                .body(Mono.just(handString), String)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Result)
                .value(allOf([
                        hasProperty("combination", equalTo(Combination.HIGH)),
                        hasProperty("hand", equalTo(hand))
                ]))

        where:
        hand = [
                new Card(2, 5),
                new Card(4, 10),
                new Card(2, 4),
                new Card(4, 3),
                new Card(4, 13),
                new Card(2, 9),
                new Card(3, 3)
        ]
        handString = hand.collect { "${it.suit},${it.rank}" }.join(",")
    }

    @Unroll
    def "When hand size less then 5 then #errorMessage error"() {
        when:
        webClient.put()
                .uri("/combination")
                .body(Mono.just("4,5"), String)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("\$.message").value(equalTo(errorMessage))

        then:
        1 * combinationFinder.getHighestCombination(_) >> { throw new IllegalArgumentException(errorMessage) }

        where:
        errorMessage = "testErrorMessage"
    }

    def "Test batch file execution"() {
        expect:
        webClient.post()
                .uri("/combination")
                .body(fromMultipartData("file", testFileResource))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Result).hasSize(10)
                .consumeWith { response ->
                    assertThat(response.responseBody.collect { it.combination }).allMatch(isEqual(Combination.HIGH))
                }
    }

    def "Test batch flux execution"() {
        given:
        def random = ThreadLocalRandom.current()
        def body = Flux
                .generate { SynchronousSink<List<Card>> handSink ->
                    Flux.generate { SynchronousSink<Card> cardSink ->
                        cardSink.next(new Card(random.nextInt(1, 5), random.nextInt(1, 13)))
                    }
                            .limitRequest(7)
                            .collectList()
                            .subscribe { handSink.next(it) }
                }
                .limitRequest(bodyLength)
                .map { hand -> hand.collect { card -> "${card.suit},${card.rank}" }.join(",") }
                .collectList()

        expect:
        webClient.patch()
                .uri("/combination")
                .body(body, List)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Result).hasSize(bodyLength)
                .consumeWith { response ->
                    assertThat(response.responseBody.collect { it.combination }).allMatch(isEqual(Combination.HIGH))
                }

        where:
        bodyLength = 10
    }
}
