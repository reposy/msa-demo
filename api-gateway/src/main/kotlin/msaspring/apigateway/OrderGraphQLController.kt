package msaspring.apigateway

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class OrderGraphQLController(private val orderQuery: OrderQuery) {

    @QueryMapping
    fun orders(): Flux<Order> {
        return orderQuery.orders()
    }
}