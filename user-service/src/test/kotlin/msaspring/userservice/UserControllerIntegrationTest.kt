package msaspring.userservice

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val userRepository: UserRepository
) {

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll().block()
    }

    @Test
    fun `should create a new user`() {
        // given
        val newUser = User(name = "Alice", email = "alice@example.com")

        // when & then: POST 요청으로 새 사용자 생성
        webTestClient.post().uri("/users")
            .bodyValue(newUser)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("Alice")
            .jsonPath("$.email").isEqualTo("alice@example.com")
    }

    @Test
    fun `should return all users`() {
        // given: 몇 건의 사용자 미리 저장
        val user1 = User(name = "Alice", email = "alice@example.com")
        val user2 = User(name = "Bob", email = "bob@example.com")
        userRepository.saveAll(listOf(user1, user2)).collectList().block()

        // when & then: GET 요청으로 모든 사용자 조회
        webTestClient.get().uri("/users")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(User::class.java)
            .hasSize(2)
    }

    @Test
    fun `should update a user`() {
        // given: 기존 사용자 저장
        val existingUser = User(name = "Alice", email = "alice@example.com")
        val savedUser = userRepository.save(existingUser).block()!!
        val updatedData = User(name = "Alice Updated", email = "alice.updated@example.com")

        // when & then: PUT 요청으로 사용자 업데이트
        webTestClient.put().uri("/users/${savedUser.id}")
            .bodyValue(updatedData)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Alice Updated")
            .jsonPath("$.email").isEqualTo("alice.updated@example.com")
    }

    @Test
    fun `should delete a user`() {
        // given: 사용자 저장 후 삭제 대상 설정
        val user = User(name = "Alice", email = "alice@example.com")
        val savedUser = userRepository.save(user).block()!!

        // when & then: DELETE 요청 실행
        webTestClient.delete().uri("/users/${savedUser.id}")
            .exchange()
            .expectStatus().isOk

        // 이후 GET 요청 시, 해당 사용자가 존재하지 않음을 확인 (404 혹은 에러)
        webTestClient.get().uri("/users/${savedUser.id}")
            .exchange()
            .expectStatus().is5xxServerError()
    }
}