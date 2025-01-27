package life.sochpekharoch.serenity.di

import life.sochpekharoch.serenity.repository.PostRepository

object Providers {
    fun providePostRepository(): PostRepository {
        return PostRepository()
    }
} 