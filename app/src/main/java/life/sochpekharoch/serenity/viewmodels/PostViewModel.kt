package life.sochpekharoch.serenity.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import life.sochpekharoch.serenity.data.Post
import android.net.Uri
import life.sochpekharoch.serenity.repository.PostRepository
import life.sochpekharoch.serenity.models.PostType

class PostViewModel(
    private val app: Application,
    private val repository: PostRepository
) : AndroidViewModel(app) {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showWelcomeMessage = MutableStateFlow(false)
    val showWelcomeMessage: StateFlow<Boolean> = _showWelcomeMessage

    init {
        checkFirstTimeUser()
        loadPosts()
    }

    private fun checkFirstTimeUser() {
        viewModelScope.launch {
            try {
                val prefs = app.getSharedPreferences("serenity_prefs", Context.MODE_PRIVATE)
                val isFirstTime = prefs.getBoolean("is_first_time", true)
                
                Log.d("WelcomeDebug", "Checking first time user: $isFirstTime")
                
                if (isFirstTime) {
                    _showWelcomeMessage.value = true
                    prefs.edit().putBoolean("is_first_time", false).apply()
                    Log.d("WelcomeDebug", "This is first time user, showing welcome message")
                } else {
                    Log.d("WelcomeDebug", "Not first time user")
                }
            } catch (e: Exception) {
                Log.e("WelcomeDebug", "Error checking first time user", e)
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                Log.d("PostDebug", "Starting to load posts...")
                _isLoading.value = true
                
                val postsCollection = FirebaseFirestore.getInstance().collection("posts")
                Log.d("PostDebug", "Got posts collection reference")
                
                val result = postsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                Log.d("PostDebug", "Firestore query completed. Number of documents: ${result.size()}")
                
                val posts = result.documents.mapNotNull { doc ->
                    Log.d("PostDebug", """
                        Raw document data:
                        - ID: ${doc.id}
                        - Fields: ${doc.data}
                    """.trimIndent())
                    
                    val post = doc.toObject(Post::class.java)
                    if (post == null) {
                        Log.e("PostDebug", "Failed to convert document ${doc.id} to Post object")
                    } else {
                        Log.d("PostDebug", "Successfully converted document ${doc.id} to Post object")
                    }
                    post?.copy(id = doc.id)
                }

                Log.d("PostDebug", "Final posts list size: ${posts.size}")
                _posts.value = posts
                _isLoading.value = false
                
            } catch (e: Exception) {
                Log.e("PostDebug", "Error loading posts", e)
                _isLoading.value = false
            }
        }
    }

    fun refreshPosts() {
        loadPosts()
    }

    fun dismissWelcomeMessage() {
        _showWelcomeMessage.value = false
    }

    fun createPost(content: String, type: PostType, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                val imageUrl = if (type == PostType.IMAGE && imageUri != null) {
                    repository.uploadImage(imageUri)
                } else null

                val post = Post(
                    content = content,
                    type = type,
                    imageUrl = imageUrl,
                    // ... other post fields
                )

                repository.createPost(post)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post", e)
            }
        }
    }
} 