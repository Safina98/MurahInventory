package com.example.tokomurahinventory.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    val wrappedObserver = object : Observer<T> {

        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    }
    observe(lifecycleOwner, wrappedObserver)
}