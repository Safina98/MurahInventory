package com.example.tokomurahinventory.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BindingAdapters {
    private const val FULL_DATE_FORMAT = "EEEE, d MMMM yyyy"

    @JvmStatic
    @BindingAdapter("logDate")
    fun bindLogDate(textView: TextView, date: Date?) {
        val sdf = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
        val dateToDisplay = date ?: Date() // Use today's date if date is null
        textView.text = sdf.format(dateToDisplay)
    }
}
