package life.sochpekharoch.serenity.models

data class PollOption(
    val id: String = "",
    val text: String = "",
    val votes: List<String> = listOf()
) 