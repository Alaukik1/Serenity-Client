package life.sochpekharoch.serenity.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun clearError() {
        _error.value = null
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setError(message: String?) {
        _error.value = message
    }
} 