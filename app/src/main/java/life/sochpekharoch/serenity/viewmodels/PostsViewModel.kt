package life.sochpekharoch.serenity.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.repository.FirebaseRepository

class PostsViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val _isReposting = MutableStateFlow(false)
    val isReposting: StateFlow<Boolean> = _isReposting

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                val fetchedPosts = repository.getPosts()
                _posts.value = fetchedPosts
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun repostPost(post: Post) {
        viewModelScope.launch {
            try {
                _isReposting.value = true
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    repository.repostPost(post, currentUser.uid)
                    // Refresh posts after repost
                    loadPosts()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isReposting.value = false
            }
        }
    }

    fun unrepostPost(originalPostId: String, repostId: String) {
        viewModelScope.launch {
            try {
                _isReposting.value = true
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    repository.unrepostPost(originalPostId, repostId, currentUser.uid)
                    // Refresh posts after unrepost
                    loadPosts()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isReposting.value = false
            }
        }
    }
} 