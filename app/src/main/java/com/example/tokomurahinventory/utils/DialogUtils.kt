package com.example.tokomurahinventory.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.viewmodels.UsersViewModel
import java.util.Date

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
    fun showCreratedEdited(context: Context,createdBy:String, lastEditedBy:String, createdDate: Date, lastEditedDate: Date){
        val builder = android.app.AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_created_edited, null)
        val textCreatedBy = view.findViewById<TextView>(R.id.textCreatedBy)
        val textLastEditedBy = view.findViewById<TextView>(R.id.textLastEditedBy)
        val textCreatedDate = view.findViewById<TextView>(R.id.textCreatedDate)
        val textLastEditedDate = view.findViewById<TextView>(R.id.textLastEditedDate)

        textCreatedBy.setText(createdBy)
        textLastEditedBy.setText(lastEditedBy)
        textCreatedDate.setText(formatDateToString(createdDate))
        textLastEditedDate.setText(formatDateToString(lastEditedDate))

        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }
}
