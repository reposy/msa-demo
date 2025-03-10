package msaspring.apigateway

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class OrderQuery {

    // 각 서비스의 REST API 엔드포인트 URL (실제 환경에 맞게 수정)
    private val orderServiceClient = WebClient.create("http://localhost:8081")
    private val userServiceClient = WebClient.create("http://localhost:8083")
    private val productServiceClient = WebClient.create("http://localhost:8082")

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