package msaspring.msadashboard

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

// GraphQL 응답의 래퍼 클래스 (필요에 따라 응답 구조에 맞게 정의)
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<Any>? = null
)

data class OrderDto(
    val id: Long?,
    val quantity: Int,
    val status: String,
    val user: UserDto?,
    val product: ProductDto?
)

data class UserDto(
    val id: Long?,
    val name: String?,
    val email: String?
)

data class ProductDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val price: Double?,
    val stock: Int?
)

data class DashboardData(
    val orders: List<OrderDto>
)

@Service
class DashboardApiClient(
    @Value("\${API_GATEWAY_URL:http://api-gateway:8084}") private val apiGatewayUrl: String
) {

    private val webClient: WebClient = WebClient.create(apiGatewayUrl)

    fun getDashboardData(): Mono<DashboardData> {
        // GraphQL 쿼리: API Gateway에서 집계된 주문 데이터를 조회
        val graphqlQuery = """
            query {
              orders {
                id
                quantity
                status
                user {
                  id
                  name
                  email
                }
                product {
                  id
                  name
                  description
                  price
                  stock
                }
              }
            }
        """.trimIndent()
        // 요청 본문은 query를 담은 JSON 객체
        val requestBody = mapOf("query" to graphqlQuery)

        return webClient.post()
            .uri("/graphql")
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<GraphQLResponse<DashboardData>>() {})
            .map { response ->
                // 만약 errors가 있다면, 로그를 남기거나 기본값 처리
                response.data ?: DashboardData(emptyList())
            }
    }
}