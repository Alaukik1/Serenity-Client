package life.sochpekharoch.serenity.bots

interface Bot {
    val botId: String
    val botName: String
    val botAvatarId: Int
    
    suspend fun shareContent()
} 