package life.sochpekharoch.serenity.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ContentApi {
    // NewsAPI for articles
    @GET("v2/everything")
    suspend fun getMentalHealthNews(
        @Query("q") query: String = "mental health OR psychology OR mindfulness",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "relevancy",
        @Query("apiKey") apiKey: String
    ): NewsResponse

    // Google Custom Search API for blog posts
    @GET("customsearch/v1")
    suspend fun getBlogPosts(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String = "mental health tips blog",
    ): SearchResponse
}

data class NewsResponse(
    val articles: List<Article>
)

data class Article(
    val title: String,
    val description: String,
    val url: String,
    val source: Source
)

data class Source(
    val name: String
)

data class SearchResponse(
    val items: List<SearchItem>
)

data class SearchItem(
    val title: String,
    val snippet: String,
    val link: String
) 