package com.example.tokomurahinventory.utils


import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("app:showIfAdmin")
    fun bindVisibilityBasedOnRole(view: View, showIfAdmin: Boolean) {
        val context = view.context
        val lifecycleOwner = context as? LifecycleOwner
        if (lifecycleOwner != null) {
            SharedPreferencesHelper.userRole.observe(lifecycleOwner, Observer { role ->
                Log.i("USERROLEPROB", "binding Adapter role $role")
                view.visibility = if (showIfAdmin && (role == UserRoles.ADMIN)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            })
        } else {
            Log.e("USERROLEPROB", "Context is not a LifecycleOwner.")
        }
    }
    @JvmStatic
    @BindingAdapter("app:showIfAdminAndNotLoading")
    fun bindVisibilityBasedOnRoleAndState(view: View, showIfAdmin: Boolean) {
        val context = view.context
        val lifecycleOwner = context as? LifecycleOwner
        if (lifecycleOwner != null) {
            SharedPreferencesHelper.userRole.observe(lifecycleOwner, Observer { role ->
                Log.i("USERROLEPROB", "binding Adapter role $role")
                view.visibility = if (!showIfAdmin && (role == UserRoles.ADMIN)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            })
        } else {
            Log.e("USERROLEPROB", "Context is not a LifecycleOwner.")
        }
    }
    @JvmStatic
    @BindingAdapter("app:showIfAdminOrEditor")
    fun bindVisibilityBasedOnRoleN(view: View, showIfAdminOrEditor: Boolean) {
        SharedPreferencesHelper.userRole.observe(view.context as LifecycleOwner, Observer { role ->
            view.visibility = if (showIfAdminOrEditor && (role == UserRoles.ADMIN || role == UserRoles.EDITOR)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }

    @JvmStatic
    @BindingAdapter("app:showIfWarnaClicked")
    fun bindVisibilityIsWarnaClicked(view: View, isWarnaClick: Boolean) {
        SharedPreferencesHelper.userRole.observe(view.context as LifecycleOwner, Observer { role ->
            view.visibility = if (isWarnaClick && (role == UserRoles.ADMIN|| role == UserRoles.EDITOR)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }
    @JvmStatic
    @BindingAdapter("app:showIfMerkClicked")
    fun bindVisibilityIsMerlClicked(view: View, isWarnaClick: Boolean) {
        SharedPreferencesHelper.userRole.observe(view.context as LifecycleOwner, Observer { role ->
            view.visibility = if (isWarnaClick && (role == UserRoles.ADMIN)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }
    @JvmStatic
    @BindingAdapter("isWarnaClick", "isMerkClick")
    fun showSearchIfMerkClick(view: View, isWarnaClick: Boolean, isMerkClick: Boolean) {
        view.visibility = if (!isWarnaClick && isMerkClick) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    @JvmStatic
    @BindingAdapter("app:hideSearchIfMerkClicked")
    fun hideSearchIfMerkClick(view: View, isWarnaClick: Boolean) {
        view.visibility = if (isWarnaClick) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }


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
