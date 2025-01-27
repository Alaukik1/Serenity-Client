package life.sochpekharoch.serenity.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarId: Int = 1,
    val isEmailVerified: Boolean = false,
    val accountStatus: String = "pending",
    val isFirstLogin: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) 