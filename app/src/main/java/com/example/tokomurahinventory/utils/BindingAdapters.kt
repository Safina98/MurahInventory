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
    @JvmStatic
    @BindingAdapter("app:textOrMerk")
    fun setTextOrMerk(textView: TextView, value: String?) {
        val defaultText = "+Merk" // Replace with your default string or use a resource
        textView.text = value ?: defaultText
    }
    @JvmStatic
    @BindingAdapter("app:textOrKode")
    fun setTextOrKode(textView: TextView, value: String?) {
        val defaultText = "+Kode" // Replace with your default string or use a resource
        textView.text = value ?: defaultText
    }
    @JvmStatic
    @BindingAdapter("app:textOrIsi")
    fun setTextOrIsi(textView: TextView, value: Double?) {
        val defaultText = "0.0" // Replace with your default string or use a resource

        textView.text = if (value!=null) value.toString() else defaultText
    }
}
