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
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class AuthViewModel : ViewModel() {

    private val _authenticationState = MutableLiveData<Boolean?>(null)
    val authenticationState: LiveData<Boolean?> get() = _authenticationState
    private val _showLoginDialog = MutableLiveData<Boolean>()
    val showLoginDialog: LiveData<Boolean> get() = _showLoginDialog
    init {
        _authenticationState.value = _authenticationState.value
    }

    fun authenticate(username: String, password: String, context: Context, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseInventory.getInstance(context).usersDao
            //val user = userDao.getUserByUsername(username)
            val user = userDao.getUserByUserNameAndPassword(username,password)
            Log.d("AppDebug", "user from view model: $user")
            val isAuthenticated = user != null //&& BCrypt.checkpw(password, user.password)
            withContext(Dispatchers.Main) {
                callback(isAuthenticated)
                if (isAuthenticated) {
                    SharedPreferencesHelper.saveUsername(context, username)
                    Log.d("AppDebug", "authenticate user: $user")
                    SharedPreferencesHelper.saveUserRole(context, user!!.usersRole)
                    setAuthenticationState(true)
                }
            }
        }
    }


    fun setAuthenticationState(isAuthenticated: Boolean?) {
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
