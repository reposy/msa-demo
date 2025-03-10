package msaspring.apigateway

data class Order(
    val id: Long? = null,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val status: String,
    // Aggregated fields
    val user: User? = null,
    val product: Product? = null
)