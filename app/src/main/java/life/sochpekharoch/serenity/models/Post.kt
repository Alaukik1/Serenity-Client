package life.sochpekharoch.serenity.models

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val type: PostType = PostType.TEXT,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = listOf(),
    val comments: List<String> = listOf(),
    val repostedBy: List<String> = listOf(),
    val pollOptions: List<PollOption> = listOf(),
    val userAvatarId: Int = 0,
    val username: String = "",
    val isRepost: Boolean = false,
    val repostUserId: String? = null,
    val repostUsername: String? = null,
    val repostUserAvatarId: Int? = null,
    val repostComment: String? = null,
    val originalUsername: String? = null,
    val originalUserAvatarId: Int? = null,
    val originalUserId: String? = null
)