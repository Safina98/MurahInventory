package com.example.tokomurahinventory.utils

import android.content.Context

object SharedPreferencesHelper {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERNAME = "username"

    fun saveUsername(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun getLoggedInUser(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    fun clearUsername(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_USERNAME)
        editor.apply()
    }
}
