package msaspring.apigateway

data class Order(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val status: String,
    val user: User? = null,
    val product: Product? = null
)