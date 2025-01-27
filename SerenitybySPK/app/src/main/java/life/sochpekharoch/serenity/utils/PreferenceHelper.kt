package life.sochpekharoch.serenity.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private const val PREF_NAME = "SerenityPrefs"
    private const val KEY_FIRST_LOGIN = "first_login"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLogin(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_LOGIN, true)
    }

    fun setFirstLoginComplete(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_LOGIN, false).apply()
    }
} 