package msaspring.orderservice

data class OrderQueryRequest(
    val queryType: String = "",
    val orderId: Long? = null,
    val count: Int? = null
)