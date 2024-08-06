package com.example.tokomurahinventory.utils

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
enum class UpdateStatus {
    SUCCESS,
    MERK_NOT_PRESENT,
    WARNA_NOT_PRESENT,
    ISI_NOT_PRESENT,
    PCS_NOT_READY_IN_STOCK,
    ITEM_NOT_FOUND
}

const val FULL_DATE_FORMAT = "EEEE, d MMMM yyyy"
val userNullString = "User kosong, log out dan log in kembali"
val viewerNotAuthorized="Viewer not authorized to add Item, switch to editor or admin"
val viewerAndEditorNotAuthorized="Viewer and Editor not authorized to edit or delete item, switch to admin"
val DATE_FORMAT = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    val wrappedObserver = object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    }
    observe(lifecycleOwner, wrappedObserver)
}
fun formatDateToString(date: Date):String{

        val sdf = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
        val dateToDisplay = date ?: Date() // Use today's date if date is null
        return sdf.format(dateToDisplay)

}