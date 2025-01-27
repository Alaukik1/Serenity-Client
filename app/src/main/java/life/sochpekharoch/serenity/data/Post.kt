package life.sochpekharoch.serenity.data

import life.sochpekharoch.serenity.models.PostType

data class Post(
    val id: String = "",
    val userId: String = "",
    val userAvatarId: Int = 1,
    val avatar: String = "avatar_1",
    val content: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val likedBy: List<String> = listOf(),
    val commentsCount: Int = 0,
    val type: PostType = PostType.TEXT,
    val options: List<Map<String, Any>>? = null,
    val username: String = ""
) 