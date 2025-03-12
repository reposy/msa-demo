package msaspring.orderservice

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface OrderRepository : ReactiveCrudRepository<Order, Long> {
    fun findAllByOrderByIdDesc(pageable: Pageable): Flux<Order>
}