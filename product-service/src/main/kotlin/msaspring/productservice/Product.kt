package msaspring.productservice

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("products")
data class Product(
    @Id
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int
)