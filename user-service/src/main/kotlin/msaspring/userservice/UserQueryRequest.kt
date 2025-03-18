package msaspring.userservice

data class UserQueryRequest(
    val queryType: String,
    val userId: Long? = null,
    val count: Int? = null
)