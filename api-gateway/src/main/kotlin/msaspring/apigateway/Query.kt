package msaspring.apigateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class Query(
    @Value("\${ORDER_SERVICE_URL:http://order-service:8081}") private val orderServiceUrl: String,
    @Value("\${USER_SERVICE_URL:http://user-service:8083}") private val userServiceUrl: String,
    @Value("\${PRODUCT_SERVICE_URL:http://product-service:8082}") private val productServiceUrl: String,
) {
    // 각 서비스의 REST API 클라이언트 생성
    private val orderServiceClient: WebClient = WebClient.create(orderServiceUrl)
    private val userServiceClient: WebClient = WebClient.create(userServiceUrl)
    private val productServiceClient: WebClient = WebClient.create(productServiceUrl)

    // 주문 조회 시, 각 주문에 대해 user와 product 정보를 함께 조회하여 결합
    fun orders(): Flux<Order> {
        return orderServiceClient.get().uri("/orders")
            .retrieve()
            .bodyToFlux(Order::class.java)
            .flatMap { order ->
                val userMono: Mono<User> = userServiceClient.get().uri("/users/${order.userId}")
                    .retrieve()
                    .bodyToMono(User::class.java)
                val productMono: Mono<Product> = productServiceClient.get().uri("/products/${order.productId}")
                    .retrieve()
                    .bodyToMono(Product::class.java)
                Mono.zip(userMono, productMono)
                    .map { tuple ->
                        order.copy(user = tuple.t1, product = tuple.t2)
                    }
            }
    }

    // Kafka 방식: 주문 조회 (최근 데이터를 Kafka 기반 엔드포인트를 사용)
    fun ordersKafka(): Flux<Order> {
        return orderServiceClient.get().uri("/orders/kafka/recent?count=1000")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Order>>() {})
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { order ->
                val userMono: Mono<User> = userServiceClient.get().uri("/users/kafka/${order.userId}")
                    .retrieve()
                    .bodyToMono(User::class.java)
                val productMono: Mono<Product> = productServiceClient.get().uri("/products/kafka/${order.productId}")
                    .retrieve()
                    .bodyToMono(Product::class.java)
                Mono.zip(userMono, productMono)
                    .map { tuple -> order.copy(user = tuple.t1, product = tuple.t2) }
            }
    }
}