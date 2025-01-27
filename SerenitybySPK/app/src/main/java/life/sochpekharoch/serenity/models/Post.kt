package life.sochpekharoch.serenity.models

data class Post(
    val id: String = "",
    val content: String = "",
    val userId: String = "",
    val userAvatarId: Int = 1,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val likedBy: List<String> = listOf(),
    val commentsCount: Int = 0,
    val type: String = "TEXT",
    val options: List<Map<String, Any>>? = null
) 