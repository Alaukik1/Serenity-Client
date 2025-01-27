package life.sochpekharoch.serenity.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import life.sochpekharoch.serenity.di.Providers

class PostViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(
                application,
                Providers.providePostRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 