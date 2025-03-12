package msaspring.apigateway

data class Order(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val status: String,
    val user: User? = null,      // Query에서 채워질 필드
    val product: Product? = null // Query에서 채워질 필드
)