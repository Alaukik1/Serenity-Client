package life.sochpekharoch.serenity.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GuardianApi {
    @GET("search")
    suspend fun getMentalHealthArticles(
        @Query("q") query: String = "mental health OR psychology OR mindfulness",
        @Query("section") section: String = "society|lifeandstyle",
        @Query("show-fields") fields: String = "headline,bodyText,thumbnail",
        @Query("api-key") apiKey: String
    ): GuardianResponse
}

data class GuardianResponse(
    val response: Response
)

data class Response(
    val results: List<GuardianArticle>
)

data class GuardianArticle(
    val id: String,
    val webTitle: String,
    val webUrl: String,
    val fields: Fields?
)

data class Fields(
    val headline: String?,
    val bodyText: String?,
    val thumbnail: String?
) 