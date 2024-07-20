package com.example.tokomurahinventory.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

object UserRoles {
    const val ADMIN = "Admin"
    const val EDITOR = "Editor"
    const val VIEWER = "Viewer"
}
object Satuan {
    const val METER = "Meter"
    const val YARD = "Yard"
}
object MASUKKELUAR {
    const val MASUK = "Masuk"
    const val KELUAR = "Keluar"
}
val userNullString = "User kosong, log out dan log in kembali"

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    val wrappedObserver = object : Observer<T> {

        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    }
    observe(lifecycleOwner, wrappedObserver)
}