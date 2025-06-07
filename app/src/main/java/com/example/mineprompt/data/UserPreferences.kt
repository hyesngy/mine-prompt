package com.example.mineprompt.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "mineprompt_user_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NICKNAME = "user_nickname"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_GUEST = "is_guest"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveUserLogin(userId: Long, nickname: String, email: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NICKNAME, nickname)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_GUEST, false)
            apply()
        }
    }

    fun setGuestLogin() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_USER_ID, -1L) // 게스트 사용자는 -1
            putString(KEY_USER_NICKNAME, "게스트")
            putString(KEY_USER_EMAIL, "")
            putBoolean(KEY_IS_GUEST, true)
            apply()
        }
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun getUserNickname(): String {
        return prefs.getString(KEY_USER_NICKNAME, "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun isGuest(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}