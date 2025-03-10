package msaspring.productservice

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val productRepository: ProductRepository
) {

    @AfterEach
    fun cleanup() {
        productRepository.deleteAll().block()
    }

    @Test
    fun `should create a new product`() {
        // given
        val newProduct = Product(name = "Widget", description = "A useful widget", price = 9.99, stock = 100)

        // when & then: POST 요청으로 새 상품 생성
        webTestClient.post().uri("/products")
            .bodyValue(newProduct)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("Widget")
            .jsonPath("$.price").isEqualTo(9.99)
            .jsonPath("$.stock").isEqualTo(100)
    }

    @Test
    fun `should return all products`() {
        // given: 몇 개의 상품 미리 저장
        val product1 = Product(name = "Widget", description = "A useful widget", price = 9.99, stock = 100)
        val product2 = Product(name = "Gadget", description = "An awesome gadget", price = 19.99, stock = 50)
        productRepository.saveAll(listOf(product1, product2)).collectList().block()

        // when & then: GET 요청으로 모든 상품 조회
        webTestClient.get().uri("/products")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Product::class.java)
            .hasSize(2)
    }

    @Test
    fun `should update a product`() {
        // given: 기존 상품 저장
        val existingProduct = Product(name = "Widget", description = "A useful widget", price = 9.99, stock = 100)
        val savedProduct = productRepository.save(existingProduct).block()!!
        val updatedData = Product(name = "Widget Plus", description = "An improved widget", price = 12.99, stock = 80)

        // when & then: PUT 요청으로 상품 업데이트
        webTestClient.put().uri("/products/${savedProduct.id}")
            .bodyValue(updatedData)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Widget Plus")
            .jsonPath("$.price").isEqualTo(12.99)
            .jsonPath("$.stock").isEqualTo(80)
    }

    @Test
    fun `should delete a product`() {
        // given: 상품 저장 후 삭제 대상 설정
        val product = Product(name = "Widget", description = "A useful widget", price = 9.99, stock = 100)
        val savedProduct = productRepository.save(product).block()!!

        // when & then: DELETE 요청 실행
        webTestClient.delete().uri("/products/${savedProduct.id}")
            .exchange()
            .expectStatus().isOk

        // 이후 GET 요청 시, 해당 상품이 존재하지 않음을 확인 (404 혹은 에러)
        webTestClient.get().uri("/products/${savedProduct.id}")
            .exchange()
            .expectStatus().is5xxServerError()
    }
}