package msaspring.apigateway

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class GraphQLController(private val service: GraphQLService) {

    // 전체 데이터 조회
    @QueryMapping
    fun users(): Flux<User> = service.getUsers()

    @QueryMapping
    fun products(): Flux<Product> = service.getProducts()

    @QueryMapping
    fun orders(): Flux<Order> = service.getOrders()

    // 최근 데이터 조회
    @QueryMapping
    fun recentUsers(@Argument count: Int): Flux<User> = service.getRecentUsers(count)

    @QueryMapping
    fun recentProducts(@Argument count: Int): Flux<Product> = service.getRecentProducts(count)

    @QueryMapping
    fun recentOrders(@Argument count: Int): Flux<Order> = service.getRecentOrders(count)

    // 대량 랜덤 데이터 생성 Mutation
    @MutationMapping
    fun generateUsers(@Argument count: Int): Flux<User> = service.generateUsers(count)

    @MutationMapping
    fun generateProducts(@Argument count: Int): Flux<Product> = service.generateProducts(count)

    @MutationMapping
    fun generateOrders(@Argument count: Int): Flux<Order> = service.generateOrders(count)
}