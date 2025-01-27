package life.sochpekharoch.serenity.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object KeyHashPrinter {
    fun printKeyHash(context: Context) {
        try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = java.security.MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val hashBytes = md.digest()
                
                // Convert to hex format with colons
                val hex = hashBytes.joinToString(":") { String.format("%02X", it) }
                Log.d("KeyHash", "SHA1: $hex")
                
                // Also print SHA-256 for additional security
                val sha256 = java.security.MessageDigest.getInstance("SHA-256")
                sha256.update(signature.toByteArray())
                val sha256Bytes = sha256.digest()
                val sha256Hex = sha256Bytes.joinToString(":") { String.format("%02X", it) }
                Log.d("KeyHash", "SHA256: $sha256Hex")
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "printHashKey()", e)
        }
    }
} 