package msaspring.apigateway

data class Product(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int
)