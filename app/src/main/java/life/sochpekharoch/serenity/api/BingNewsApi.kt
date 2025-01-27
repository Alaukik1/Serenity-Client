package life.sochpekharoch.serenity.api

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BingNewsApi {
    @Headers(
        "Ocp-Apim-Subscription-Key: {your_api_key}"
    )
    @GET("news/search")
    suspend fun searchNews(
        @Query("q") query: String = "mental health OR psychology OR mindfulness",
        @Query("count") count: Int = 10,
        @Query("mkt") market: String = "en-US",
        @Query("freshness") freshness: String = "Day"
    ): BingNewsResponse
}

data class BingNewsResponse(
    val value: List<NewsArticle>
)

data class NewsArticle(
    val name: String,
    val description: String,
    val url: String,
    val provider: List<Provider>,
    val datePublished: String
)

data class Provider(
    val name: String
) 