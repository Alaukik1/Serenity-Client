package life.sochpekharoch.serenity.models

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userAvatarId: Int = 1,
    val content: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val likedBy: List<String> = listOf()
) 