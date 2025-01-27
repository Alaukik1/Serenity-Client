package life.sochpekharoch.serenity.services

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

class VerificationService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("verification_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val CODE_EXPIRY_TIME = 5 * 60 * 1000 // 5 minutes in milliseconds
        private const val MAX_ATTEMPTS = 3
    }

    fun generateAndStoreCode(phoneNumber: String): String {
        val code = (100000..999999).random().toString()
        val currentTime = System.currentTimeMillis()
        
        prefs.edit().apply {
            putString("code_$phoneNumber", code)
            putLong("expiry_$phoneNumber", currentTime + CODE_EXPIRY_TIME)
            putInt("attempts_$phoneNumber", 0)
            apply()
        }
        
        return code
    }

    fun verifyCode(phoneNumber: String, code: String): Boolean {
        val storedCode = prefs.getString("code_$phoneNumber", null)
        val expiryTime = prefs.getLong("expiry_$phoneNumber", 0)
        val attempts = prefs.getInt("attempts_$phoneNumber", 0)
        val currentTime = System.currentTimeMillis()

        // Check attempts
        if (attempts >= MAX_ATTEMPTS) {
            throw Exception("Too many attempts. Please request a new code.")
        }

        // Increment attempts
        prefs.edit().putInt("attempts_$phoneNumber", attempts + 1).apply()

        // Check expiry
        if (currentTime > expiryTime) {
            throw Exception("Verification code has expired. Please request a new code.")
        }

        return code == storedCode
    }

    fun clearVerificationData(phoneNumber: String) {
        prefs.edit().apply {
            remove("code_$phoneNumber")
            remove("expiry_$phoneNumber")
            remove("attempts_$phoneNumber")
            apply()
        }
    }
} 