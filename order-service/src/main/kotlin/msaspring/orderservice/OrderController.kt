package msaspring.orderservice

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/orders")
class OrderController(private val orderRepository: OrderRepository) {

    // 모든 주문 조회 (Flux)
    @GetMapping
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

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
}