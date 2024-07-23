package com.example.tokomurahinventory.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.viewmodels.UsersViewModel

object DialogUtils {
    fun showConfirmationDialog(
        context: Context,
        viewModel: UsersViewModel,
        item: UsersTable,
        onConfirm: (UsersViewModel, UsersTable) -> Unit
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Konfirmasi")
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus ${item.userName}?")
        dialogBuilder.setPositiveButton("Hapus") { _, _ ->
            onConfirm(viewModel, item)
        }
        dialogBuilder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
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
