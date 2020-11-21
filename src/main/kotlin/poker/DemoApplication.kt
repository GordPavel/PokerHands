package poker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

@SpringBootApplication
open class DemoApplication {
    @Bean
    open fun controllerScheduler(): Scheduler = Schedulers.newParallel("combinations-batch", 8)
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}