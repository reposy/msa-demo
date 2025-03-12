package msaspring.productservice

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ProductRepository : ReactiveCrudRepository<Product, Long> {
    fun findAllByOrderByIdDesc(pageable: Pageable): Flux<Product>
}