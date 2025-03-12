package msaspring.userserver

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface UserRepository : ReactiveCrudRepository<User, Long> {
    fun findAllByOrderByIdDesc(pageable: Pageable): Flux<User>
}