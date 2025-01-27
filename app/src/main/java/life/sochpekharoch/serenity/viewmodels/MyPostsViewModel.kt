package life.sochpekharoch.serenity.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.repository.FirebaseRepository

class MyPostsViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedPostType = MutableStateFlow<PostType?>(null)  // null means show all
    val selectedPostType: StateFlow<PostType?> = _selectedPostType

    private val _isReposting = MutableStateFlow(false)
    val isReposting: StateFlow<Boolean> = _isReposting

    init {
        loadUserPosts()
    }

    fun setPostTypeFilter(type: PostType?) {
        _selectedPostType.value = type
        loadUserPosts()  // Reload posts with new filter
    }

    fun loadUserPosts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid
                
                if (userId != null) {
                    val allPosts = repository.getUserPosts(userId)
                    // Filter posts based on selected type
                    _posts.value = when (selectedPostType.value) {
                        null -> allPosts  // Show all posts
                        else -> allPosts.filter { it.type == selectedPostType.value }
                    }
                } else {
                    _error.value = "User not logged in"
                }
            } catch (e: Exception) {
                Log.e("MyPostsDebug", "Error loading posts", e)
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                // Refresh posts after deletion
                loadUserPosts()
            } catch (e: Exception) {
                Log.e("MyPostsViewModel", "Error deleting post", e)
                _error.value = "Failed to delete post: ${e.message}"
            }
        }
    }

    fun refreshPosts() {
        loadUserPosts()
    }

    fun repostPost(post: Post) {
        viewModelScope.launch {
            try {
                _isReposting.value = true
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    repository.repostPost(post, currentUser.uid)
                    refreshPosts()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isReposting.value = false
            }
        }
    }
} 