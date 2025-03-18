package msaspring.productservice

data class ProductQueryRequest(
    val queryType: String, // "findById" 또는 "getRecent"
    val productId: Long? = null,
    val count: Int? = null
)