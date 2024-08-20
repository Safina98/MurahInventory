package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.DeleteUsersClickListener
import com.example.tokomurahinventory.adapters.UpdateUsersClickListener
import com.example.tokomurahinventory.adapters.UsersAdapter
import com.example.tokomurahinventory.adapters.UsersClickListener
import com.example.tokomurahinventory.adapters.UsersLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentUsersBinding
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.UserAction
import com.example.tokomurahinventory.viewmodels.UsersViewModel
import com.example.tokomurahinventory.viewmodels.UsersViewModelFactory
import com.google.android.material.textfield.TextInputLayout


class UsersFragment : AuthFragment() {
    private lateinit var binding: FragmentUsersBinding
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UsersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_users,container,false)
        val application = requireNotNull(this.activity).application
        val usersDao = DatabaseInventory.getInstance(application).usersDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = UsersViewModelFactory(usersDao,loggedInUser,application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(UsersViewModel::class.java)
        binding.viewModel = viewModel

         adapter  = UsersAdapter(
            UsersClickListener {
            },
           UsersLongListener {
            },
            UpdateUsersClickListener {
                viewModel.canUserPerformAction(requireContext(), UserAction.DELETE) { canPerformAction ->
                    if (canPerformAction) {
                        // Proceed with delete action
                        showAddUserDialog(viewModel,it,1)
                    } else {
                        // Show error or deny access
                        Toast.makeText(context, "You don't have permission to perform this action", Toast.LENGTH_SHORT).show()
                    }
                }
                /*
                viewModel.canUserDeleteOrUpdate(requireContext()) { canDeleteOrUpdate ->
                    if (canDeleteOrUpdate) {
                        showAddUserDialog(viewModel,it,1)
                    } else {
                        Toast.makeText(context, "You don't have permission to perform this action", Toast.LENGTH_SHORT).show()
                    }
                }

                 */

            },
            DeleteUsersClickListener {
                //viewModel.deleteUser(it)
                        DialogUtils.showDeleteDialog(
                            this,
                            viewModel,
                            it,
                            { vm, item ->
                                val a = item as UsersTable
                                val isUserDeletingItSelf = (vm as UsersViewModel).checkIfUserDeletingItSelf(a.userName)
                                if (isUserDeletingItSelf) {
                                    DialogUtils.showConfirmationDialog(
                                        requireContext(), vm, a, {
                                                confirmedViewModel, confirmedItem ->

                                            (confirmedViewModel ).deleteUser(confirmedItem)
                                        }
                                    )
                                } else {
                                    (vm).deleteUser(item)
                                }
                            }
                        )
                    }

        )
        binding.rvUsers.adapter = adapter

        //adapter.submitList(viewModel.dummyModel)

        binding.searchBarUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterUsers(newText)
                return true
            }
        })

        viewModel.usersList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it.sortedBy { it.userName })
            adapter.notifyDataSetChanged()
        })
        viewModel.addUserFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                showAddUserDialog(viewModel,null,-1)
                viewModel.onAddUserFabClicked()
            }
        })

        return binding.root
    }

    fun showAddUserDialog(viewModel: UsersViewModel, usersTable: UsersTable?, i: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Pengguna")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_login, null)
        val textNama = view.findViewById<EditText>(R.id.etUsername)
        val textPassword = view.findViewById<EditText>(R.id.etPassword)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val textfgpass = view.findViewById<TextView>(R.id.txt_forget_password)
        val btn = view.findViewById<Button>(R.id.btnLogin)
        val txtLogin = view.findViewById<TextView>(R.id.tvLogin)
        val spinnerRole = view.findViewById<Spinner>(R.id.spinner_role)
        btn.visibility = View.GONE
        txtLogin.visibility = View.GONE
        textfgpass.visibility = View.GONE
        // Handle user data
        if (usersTable != null) {
            textNama.setText(usersTable.userName)
            textPassword.setText(usersTable.password)
            val userRoleArray = resources.getStringArray(R.array.user_role)
            val userRoleIndex = userRoleArray.indexOf(usersTable.usersRole)  // Assuming `role` is the property of `UsersTable`
            if (userRoleIndex >= 0) {
                spinnerRole.setSelection(userRoleIndex)
            }
        }
        // Set up the password visibility toggle
        passwordInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        builder.setView(view)
        builder.setPositiveButton("OK") { _, _ ->

            val nama = textNama.text.toString().trim().lowercase()
            val password = textPassword.text.toString().trim()
            val selectedRole = spinnerRole.selectedItem.toString()

            if (usersTable == null) {
                if (password!="" && nama !="") viewModel.insertUser(nama, password,selectedRole)
            } else {
                if (nama!="") usersTable.userName = nama
                if (password!="") usersTable.password = password
                usersTable.usersRole = selectedRole
                viewModel.updateUser(usersTable)
            }
            adapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }


}