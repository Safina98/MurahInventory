package com.example.tokomurahinventory.utils

object LogoutManager {
    var onLogout: (() -> Unit)? = null

    fun performLogout() {
        onLogout?.invoke()
    }
}
