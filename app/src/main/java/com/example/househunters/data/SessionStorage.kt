package com.example.househunters.data

import android.content.Context

class SessionStorage(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readToken(): String? = preferences.getString(KEY_TOKEN, null)

    fun writeToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val PREFS_NAME = "househunters_session"
        const val KEY_TOKEN = "jwt_token"
    }
}
