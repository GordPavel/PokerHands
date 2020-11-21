package poker


import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import poker.combinationfinder.CombinationFinder
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty
import static org.hamcrest.core.AllOf.allOf
import static org.hamcrest.core.IsEqual.equalTo
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ControllerTest extends Specification {

    @SpringBean
    CombinationFinder combinationFinder = Mock() {
        _ * getHighestCombination(_) >> { arguments -> new Result(Combination.HIGH, arguments[0] as List<Card>) }
    }

    @Autowired
    WebTestClient webClient

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

}
