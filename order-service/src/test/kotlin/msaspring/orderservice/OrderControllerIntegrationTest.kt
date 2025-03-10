package msaspring.orderservice

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val orderRepository: OrderRepository
) {

    // 테스트 후 DB 정리를 위해
    @AfterEach
    fun cleanup() {
        orderRepository.deleteAll().block()
    }

    @Test
    fun `should create a new order`() {
        // given
        val newOrder = Order(userId = 1, productId = 1, quantity = 5, status = OrderStatus.NEW)

        // when & then: POST 요청으로 새 주문 생성
        webTestClient.post().uri("/orders")
            .bodyValue(newOrder)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.userId").isEqualTo(1)
            .jsonPath("$.quantity").isEqualTo(5)
    }

    @Test
    fun `should return all orders`() {
        // given: 미리 몇 건의 주문을 저장
        val order1 = Order(userId = 1, productId = 1, quantity = 2, status = OrderStatus.NEW)
        val order2 = Order(userId = 2, productId = 2, quantity = 3, status = OrderStatus.PROCESSING)
        orderRepository.saveAll(listOf(order1, order2)).collectList().block()

        // when & then: GET 요청으로 모든 주문 조회
        webTestClient.get().uri("/orders")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Order::class.java)
            .hasSize(2)
    }

    @Test
    fun `should get an order by id`() {
        // given: 주문 저장 후 ID 확인
        val order = Order(userId = 1, productId = 1, quantity = 2, status = OrderStatus.NEW)
        val savedOrder = orderRepository.save(order).block()!!

        // when & then: 해당 ID의 주문 조회
        webTestClient.get().uri("/orders/${savedOrder.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(savedOrder.id!!)
            .jsonPath("$.userId").isEqualTo(1)
    }

    @Test
    fun `should update an order`() {
        // given: 기존 주문 저장
        val existingOrder = Order(userId = 1, productId = 1, quantity = 2, status = OrderStatus.NEW)
        val savedOrder = orderRepository.save(existingOrder).block()!!
        val updatedData = Order(userId = 1, productId = 1, quantity = 10, status = OrderStatus.COMPLETED)

        // when & then: PUT 요청으로 주문 업데이트
        webTestClient.put().uri("/orders/${savedOrder.id}")
            .bodyValue(updatedData)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.quantity").isEqualTo(10)
            .jsonPath("$.status").isEqualTo("COMPLETED")
    }

    @Test
    fun `should delete an order`() {
        // given: 주문 저장 후 삭제 대상 설정
        val order = Order(userId = 1, productId = 1, quantity = 2, status = OrderStatus.NEW)
        val savedOrder = orderRepository.save(order).block()!!

        // when & then: DELETE 요청 실행
        webTestClient.delete().uri("/orders/${savedOrder.id}")
            .exchange()
            .expectStatus().isOk

        // 이후 조회 시, 주문이 없음을 확인
        webTestClient.get().uri("/orders/${savedOrder.id}")
            .exchange()
            .expectStatus().is5xxServerError()  // 존재하지 않으면 에러 응답
    }
}