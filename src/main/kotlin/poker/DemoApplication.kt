package poker

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Clock

@SpringBootApplication
@ConfigurationPropertiesScan
open class DemoApplication {
    @Bean
    open fun controllerScheduler(
        @Value("\${combinations.batch.threadPoolName:combinations-batch}") combinationsBatchThreadPoolName: String,
        @Value("\${combinations.batch.threadsCount:8}") combinationsBatchThreadsCount: Int
    ): Scheduler = Schedulers.newParallel(
        combinationsBatchThreadPoolName,
        combinationsBatchThreadsCount
    )

    @Bean
    open fun clock(): Clock = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}