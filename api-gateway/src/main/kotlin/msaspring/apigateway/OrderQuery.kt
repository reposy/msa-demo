package msaspring.apigateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class OrderQuery (
    @Value("\${ORDER_SERVICE_URL}") private val orderServiceUrl: String,
    @Value("\${USER_SERVICE_URL}") private val userServiceUrl: String,
    @Value("\${PRODUCT_SERVICE_URL}") private val productServiceUrl: String
) {

    // 각 서비스의 REST API 엔드포인트 URL (실제 환경에 맞게 수정)
    private val orderServiceClient: WebClient = WebClient.create(orderServiceUrl)
    private val userServiceClient: WebClient = WebClient.create(userServiceUrl)
    private val productServiceClient: WebClient = WebClient.create(productServiceUrl)

    // orders 쿼리를 처리하는 리졸버
    fun orders(): Flux<Order> {
        return orderServiceClient.get().uri("/orders")
            .retrieve()
            .bodyToFlux(Order::class.java)
            .flatMap { order ->
                val userMono = userServiceClient.get().uri("/users/${order.userId}")
                    .retrieve()
                    .bodyToMono(User::class.java)
                val productMono = productServiceClient.get().uri("/products/${order.productId}")
                    .retrieve()
                    .bodyToMono(Product::class.java)
                Mono.zip(userMono, productMono)
                    .map { tuple ->
                        order.copy(user = tuple.t1, product = tuple.t2)
                    }
            }
    }
}