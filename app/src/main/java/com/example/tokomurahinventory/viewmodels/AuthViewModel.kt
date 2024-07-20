package com.example.tokomurahinventory.viewmodels
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UserRoles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class AuthViewModel : ViewModel() {

    private val _authenticationState = MutableLiveData<Boolean?>()
    val authenticationState: LiveData<Boolean?> get() = _authenticationState

    init {
        _authenticationState.value = null
    }

    fun authenticate(username: String, password: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseInventory.getInstance(context).usersDao
            val user = userDao.getUserByUsername(username)
            Log.d("AppDebug", "user from view model: $user")
            if (user != null && BCrypt.checkpw(password, user.password)) {
                _authenticationState.postValue(true)
                SharedPreferencesHelper.saveUsername(context, username)
                SharedPreferencesHelper.saveUserRole(context, user.usersRole)
            } else {
                _authenticationState.postValue(false)
            }
        }
    }

    fun setAuthenticationState(isAuthenticated: Boolean) {
        _authenticationState.value = isAuthenticated
    }

    fun checkAndInsertDefaultUser(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseInventory.getInstance(context).usersDao
            val userCount = userDao.getUserCount()
            if (userCount == 0) {
                val defaultUser = UsersTable(userName = "admin", password = hashPassword("1111"), usersRef = "adminRef", usersRole = UserRoles.ADMIN)
                userDao.insertUser(defaultUser)
            }
        }
    }


    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun isAuthenticated(): Boolean {
        return authenticationState.value == true
    }
}
