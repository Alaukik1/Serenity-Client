package life.sochpekharoch.serenity.auth

import android.app.Activity
import com.google.firebase.auth.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class PhoneAuthHandler(private val auth: FirebaseAuth) {
    private val _state = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val state: StateFlow<PhoneAuthState> = _state

    fun startVerification(phoneNumber: String, activity: Activity) {
        _state.value = PhoneAuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _state.value = PhoneAuthState.VerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val error = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid phone number"
                    is FirebaseAuthException -> "Too many requests. Try again later"
                    else -> "Verification failed: ${e.message}"
                }
                _state.value = PhoneAuthState.Error(error)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _state.value = PhoneAuthState.CodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(verificationId: String, code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            _state.value = PhoneAuthState.VerificationCompleted(credential)
        } catch (e: Exception) {
            _state.value = PhoneAuthState.Error("Invalid verification code")
        }
    }
}

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object Loading : PhoneAuthState()
    data class CodeSent(val verificationId: String) : PhoneAuthState()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneAuthState()
    data class Error(val message: String) : PhoneAuthState()
} 