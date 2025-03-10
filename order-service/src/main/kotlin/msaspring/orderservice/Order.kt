package msaspring.orderservice

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("orders")
data class Order(
    @Id
    val id: Long? = null,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val status: OrderStatus = OrderStatus.NEW
)

enum class OrderStatus {
    NEW,
    PROCESSING,
    COMPLETED,
    CANCELLED
}