package com.example.tokomurahinventory.utils

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

object DialogUtils {
    fun showDeleteDialog(
        fragment: Fragment,
        viewModel: ViewModel,
        item: Any,
        onDelete: (ViewModel, Any) -> Unit
    ) {
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setMessage("Are you sure you want to delete?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, id ->
                onDelete(viewModel, item)
            }
            .setNegativeButton("No") { dialog, id -> dialog.dismiss() }
            .setOnCancelListener { dialog -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
}
