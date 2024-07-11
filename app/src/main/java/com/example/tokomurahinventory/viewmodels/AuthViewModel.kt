package com.example.tokomurahinventory.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authenticationState = MutableLiveData<Boolean>(false)
    val authenticationState: LiveData<Boolean> get() = _authenticationState

    fun authenticate(username: String, password: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseInventory.getInstance(context).usersDao
            val userCount = userDao.getUser(username, password)

            if (userCount > 0) {
                _authenticationState.postValue(true)
                SharedPreferencesHelper.saveUsername(context, username)
            } else {
                _authenticationState.postValue(false)
            }
        }
    }
}
