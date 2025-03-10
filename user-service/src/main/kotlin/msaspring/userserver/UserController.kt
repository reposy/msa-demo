package msaspring.userserver

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository) {
    // 모든 사용자 반환 (Flux)
    @GetMapping
    fun getAllUsers(): Flux<User> = userRepository.findAll()

    // 새 사용자 생성 (Mono)
    @PostMapping
    fun createUser(@RequestBody user: User): Mono<User> = userRepository.save(user)

    // 특정 사용자 조회 (Mono)
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): Mono<User> =
        userRepository.findById(id)
            .switchIfEmpty(Mono.error(RuntimeException("User not found with id: $id")))

    // 사용자 업데이트 (Mono)
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody updatedUser: User): Mono<User> {
        return userRepository.findById(id).flatMap { existingUser ->
            val userToUpdate = existingUser.copy(
                name = updatedUser.name,
                email = updatedUser.email
            )
            userRepository.save(userToUpdate)
        }
    }

    // 사용자 삭제 (Mono<Void>)
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): Mono<Void> = userRepository.deleteById(id)

}