package msaspring.orderservice

data class OrderQueryRequest(
    val queryType: String, // "findById" 또는 "getRecent"
    val orderId: Long? = null,
    val count: Int? = null
)