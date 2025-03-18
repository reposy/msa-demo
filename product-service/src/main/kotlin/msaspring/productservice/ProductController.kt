package msaspring.productservice

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/products")
class ProductController(
    private val productRepository: ProductRepository,
    private val kafkaProductQueryService: KafkaProductQueryService
) {

    @GetMapping
    fun getAllProducts(): Flux<Product> = productRepository.findAll()

    @GetMapping("/recent")
    fun getRecentProducts(@RequestParam count: Int): Flux<Product> {
        return productRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
    }

    @PostMapping("/generate")
    fun generateProducts(@RequestParam count: Int): Flux<Product> {
        val random = Random()
        return Flux.range(1, count)
            .map {
                Product(
                    id = null,
                    name = "Product-${UUID.randomUUID()}",
                    description = "Description ${UUID.randomUUID()}",
                    price = random.nextDouble() * (100.0 - 10.0),
                    stock = random.nextInt(1, 50)
                )
            }
            .flatMap { productRepository.save(it) }
    }

    // 새로운 상품 생성 (Mono)
    @PostMapping
    fun createProduct(@RequestBody product: Product): Mono<Product> = productRepository.save(product)

    // 특정 상품 조회 (Mono)
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): Mono<Product> =
        productRepository.findById(id)
            .switchIfEmpty(Mono.error(RuntimeException("Product not found with id: $id")))

    // 상품 업데이트 (Mono)
    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: Long, @RequestBody updatedProduct: Product): Mono<Product> {
        return productRepository.findById(id).flatMap { existingProduct ->
            val productToUpdate = existingProduct.copy(
                name = updatedProduct.name,
                description = updatedProduct.description,
                price = updatedProduct.price,
                stock = updatedProduct.stock
            )
            productRepository.save(productToUpdate)
        }
    }

    // 상품 삭제 (Mono<Void>)
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): Mono<Void> = productRepository.deleteById(id)

    // --- Kafka 요청–응답 엔드포인트 ---

    // 제품 ID로 조회하는 Kafka 엔드포인트
    @GetMapping("/kafka/{id}")
    fun getProductByIdKafka(@PathVariable id: Long): Mono<Product> =
        kafkaProductQueryService.queryProductById(id)

    // 최근 제품 목록(기본 10개)을 조회하는 Kafka 엔드포인트
    @GetMapping("/kafka/recent")
    fun getRecentProductsKafka(@RequestParam(required = false, defaultValue = "10") count: Int): Mono<List<Product>> =
        kafkaProductQueryService.queryRecentProducts(count)
}