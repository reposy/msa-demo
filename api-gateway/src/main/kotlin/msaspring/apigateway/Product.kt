package msaspring.apigateway

data class Product(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val stock: Int? = null
)