package com.example.tokomurahinventory.utils



import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SharedPreferencesHelper {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_ROLE = "userRole"

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> get() = _userRole

    fun initialize(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _userRole.value = sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    fun saveUsername(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun saveUserRole(context: Context, userRole: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            Log.d("AppDebug", "save user role : $userRole")
            putString(KEY_USER_ROLE, userRole)
            Log.d("AppDebug", "KEY USER ROLE : $KEY_USER_ROLE")
            apply()
        }
        _userRole.value = userRole  // Update LiveData
        Log.d("AppDebug", "_usetROle : ${_userRole.value}")
    }

    fun getLoggedInUser(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun getUserRole(context: Context): String? {
        return _userRole.value
    }

    fun clearUsername(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(KEY_USERNAME)
            remove(KEY_USER_ROLE)
            apply()
        }
        _userRole.value = null  // Update LiveData
    }
}
