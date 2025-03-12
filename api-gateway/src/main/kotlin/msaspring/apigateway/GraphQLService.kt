package msaspring.apigateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Service
class GraphQLService(
    @Value("\${USER_SERVICE_URL:http://user-service:8083}") private val userServiceUrl: String,
    @Value("\${PRODUCT_SERVICE_URL:http://product-service:8082}") private val productServiceUrl: String,
    @Value("\${ORDER_SERVICE_URL:http://order-service:8081}") private val orderServiceUrl: String,
    private val webClientBuilder: WebClient.Builder
) {

    // 전체 데이터 조회
    fun getUsers(): Flux<User> = webClientBuilder.build()
        .get()
        .uri("$userServiceUrl/users")
        .retrieve()
        .bodyToFlux(User::class.java)

    fun getProducts(): Flux<Product> = webClientBuilder.build()
        .get()
        .uri("$productServiceUrl/products")
        .retrieve()
        .bodyToFlux(Product::class.java)

    fun getOrders(): Flux<Order> = webClientBuilder.build()
        .get()
        .uri("$orderServiceUrl/orders")
        .retrieve()
        .bodyToFlux(Order::class.java)

    // 최근 데이터 조회 (각 마이크로서비스의 /recent 엔드포인트)
    fun getRecentUsers(count: Int): Flux<User> = webClientBuilder.build()
        .get()
        .uri("$userServiceUrl/users/recent?count={count}", count)
        .retrieve()
        .bodyToFlux(User::class.java)

    fun getRecentProducts(count: Int): Flux<Product> = webClientBuilder.build()
        .get()
        .uri("$productServiceUrl/products/recent?count={count}", count)
        .retrieve()
        .bodyToFlux(Product::class.java)

    fun getRecentOrders(count: Int): Flux<Order> = webClientBuilder.build()
        .get()
        .uri("$orderServiceUrl/orders/recent?count={count}", count)
        .retrieve()
        .bodyToFlux(Order::class.java)

    // Mutation: 대량 랜덤 데이터 생성 (각 마이크로서비스의 /generate 엔드포인트)
    fun generateUsers(count: Int): Flux<User> = webClientBuilder.build()
        .post()
        .uri("$userServiceUrl/users/generate?count={count}", count)
        .retrieve()
        .bodyToFlux(User::class.java)

    fun generateProducts(count: Int): Flux<Product> = webClientBuilder.build()
        .post()
        .uri("$productServiceUrl/products/generate?count={count}", count)
        .retrieve()
        .bodyToFlux(Product::class.java)

    fun generateOrders(count: Int): Flux<Order> = webClientBuilder.build()
        .post()
        .uri("$orderServiceUrl/orders/generate?count={count}", count)
        .retrieve()
        .bodyToFlux(Order::class.java)
}