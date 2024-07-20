package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UserRoles
import kotlinx.coroutines.launch

abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    fun canUserDeleteOrUpdate(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userRole = SharedPreferencesHelper.getUserRole(context)
            if (userRole != null) {
                onResult(userRole == UserRoles.ADMIN ||userRole == UserRoles.EDITOR)
            } else {
                onResult(false)
            }
        }
    }
}
