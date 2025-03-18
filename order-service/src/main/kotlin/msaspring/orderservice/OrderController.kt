package msaspring.orderservice

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderRepository: OrderRepository,
    private val kafkaOrderQueryService: KafkaOrderQueryService
) {

    // 모든 주문 조회 (Flux)
    @GetMapping
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    // 최근 생성된 주문 상위 'count'개 반환
    @GetMapping("/recent")
    fun getRecentOrders(@RequestParam count: Int): Flux<Order> {
        return orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
    }

    @PostMapping("/generate")
    fun generateOrders(@RequestParam count: Int): Flux<Order> {
        val random = Random()
        return Flux.range(1, count)
            .map {
                Order(
                    id = null,
                    userId = random.nextLong(1, 1000),  // 실제 존재 여부와 상관없이 랜덤 숫자 할당
                    productId = random.nextLong(1, 1000),
                    quantity = random.nextInt(1, 10),
                    status = OrderStatus.NEW
                )
            }
            .flatMap { orderRepository.save(it) }
    }

    // 새로운 주문 생성 (Mono)
    @PostMapping
    fun createOrder(@RequestBody order: Order): Mono<Order> = orderRepository.save(order)

    // 특정 주문 조회 (Mono)
    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): Mono<Order> =
        orderRepository.findById(id)
            .switchIfEmpty(Mono.error(RuntimeException("Order not found with id: $id")))

    // 주문 업데이트 (Mono)
    @PutMapping("/{id}")
    fun updateOrder(@PathVariable id: Long, @RequestBody updatedOrder: Order): Mono<Order> {
        return orderRepository.findById(id).flatMap { existingOrder ->
            val orderToUpdate = existingOrder.copy(
                userId = updatedOrder.userId,
                productId = updatedOrder.productId,
                quantity = updatedOrder.quantity,
                status = updatedOrder.status
            )
            orderRepository.save(orderToUpdate)
        }
    }

    // 주문 삭제 (Mono<Void>)
    @DeleteMapping("/{id}")
    fun deleteOrder(@PathVariable id: Long): Mono<Void> = orderRepository.deleteById(id)

    // --- Kafka 요청–응답 엔드포인트 ---

    // 주문 ID로 조회하는 Kafka 엔드포인트
    @GetMapping("/kafka/{id}")
    fun getOrderByIdKafka(@PathVariable id: Long): Mono<Order> =
        kafkaOrderQueryService.queryOrderById(id)

    // 최근 주문 10개(또는 count 개)를 조회하는 Kafka 엔드포인트
    @GetMapping("/kafka/recent")
    fun getRecentOrdersKafka(@RequestParam(required = false, defaultValue = "10") count: Int): Mono<List<Order>> =
        kafkaOrderQueryService.queryRecentOrders(count)
}