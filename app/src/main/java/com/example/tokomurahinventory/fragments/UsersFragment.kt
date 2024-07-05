package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.example.tokomurahinventory.viewmodels.UsersViewModel
import com.example.tokomurahinventory.viewmodels.UsersViewModelFactory


class UsersFragment : Fragment() {
    private lateinit var binding: FragmentUsersBinding
    private val viewModel: UsersViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_users,container,false)
        val application = requireNotNull(this.activity).application
        val usersDao = DatabaseInventory.getInstance(application).usersDao
        val viewModelFactory = UsersViewModelFactory(usersDao,application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(UsersViewModel::class.java)
        binding.viewModel = viewModel
        val adapter  = UsersAdapter(
            UsersClickListener {
            },
           UsersLongListener {

            },
            UpdateUsersClickListener {
                showAddUserDialog(viewModel,it,1)
            },
            DeleteUsersClickListener {
                viewModel.deleteUser(it)
            }
        )
        binding.rvUsers.adapter = adapter

        //adapter.submitList(viewModel.dummyModel)

        viewModel.usersList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
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

    fun showAddUserDialog(viewModel: UsersViewModel, usersTable: UsersTable?,i:Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Pengguna")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_add_warna, null)
        val textNama = view.findViewById<EditText>(R.id.txt_warna)
        val textPassword = view.findViewById<EditText>(R.id.txt_satuan)
        textNama.setHint("Nama")
        textPassword.setHint("Password")
        if (usersTable!=null){
            textNama.setText(usersTable.userName)
            textPassword.setText(usersTable.passrord)
        }
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val nama = textNama.text.toString().toUpperCase()
            val password = textPassword.text.toString().toUpperCase()
            if (usersTable==null){
                viewModel.insertUser(nama,password)
            }else
            {
                usersTable.userName= nama
                usersTable.passrord=password
                viewModel.updateUser(usersTable)
            }

        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }

}