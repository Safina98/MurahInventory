package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.DeleteMerkClickListener
import com.example.tokomurahinventory.adapters.MerkAdapter
import com.example.tokomurahinventory.adapters.MerkClickListener
import com.example.tokomurahinventory.adapters.MerkLongListener
import com.example.tokomurahinventory.adapters.UpdateMerkClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentMerkBinding
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import com.example.tokomurahinventory.viewmodels.MerkViewModelFactory


class MerkFragment : AuthFragment() {
    private lateinit var binding: FragmentMerkBinding
    private val viewModel:MerkViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_merk,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource1 = DatabaseInventory.getInstance(application).merkDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?:""
        val viewModelFactory = MerkViewModelFactory(dataSource1,loggedInUser,application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(MerkViewModel::class.java)
        binding.viewModel = viewModel

        val adapter  = MerkAdapter(
            MerkClickListener {
                    viewModel.onNavigateToWarna(it.refMerk)
                },
            MerkLongListener {
                    // Handle item long click
                },
            UpdateMerkClickListener{
                showAddDialog(viewModel,it,1)
            },
            DeleteMerkClickListener{
                viewModel.deleteMerk(it)
            }
            )

        binding.rvMerk.adapter = adapter

        //showLoginDialog()

        //Observe all merk from db
        viewModel.allMerkTable.observe(viewLifecycleOwner, Observer {
            it.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()

            }
        })

        binding.searchBarMerk.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterMerk(newText)
                return true
            }
        })
        //Observe fab merk state
        viewModel.addMerkFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                showAddDialog(viewModel,null,-1)
                viewModel.onAddMerkFabClicked()
            }
        })

        //On rv click navigate to fragment warna
        viewModel.navigateToWarna.observe(viewLifecycleOwner, Observer {
            if (it!=null){
               // this.findNavController().navigate(BrandStockFragmentDirections.actionBrandStockFragmentToProductStockFragment(id))
                this.findNavController().navigate(MerkFragmentDirections.actionMerkFragmentToWarnaFragment(it))
                viewModel.onNavigatetedToWarna()
        }
        })
        return binding.root
    }

    fun showAddDialog(viewModel: MerkViewModel, merkTable:MerkTable?, i:Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Merk Barang")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_add_item, null)
        val textBrand = view.findViewById<EditText>(R.id.txt_merk)
       if (merkTable!=null)(
           textBrand.setText(merkTable.namaMerk)
       )
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val merk = textBrand.text.toString().toUpperCase()
            if (merkTable==null){
                viewModel.insertMerk(merk)
            }else
            {
                merkTable.namaMerk = merk
                viewModel.updateMerk(merkTable)
            }
        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }
    fun showLoginDialog(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Merk Barang")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_login, null)
        val textUserName = view.findViewById<EditText>(R.id.etUsername)
        val textUserPassword = view.findViewById<EditText>(R.id.etPassword)

        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val user = textUserName.text.toString().toUpperCase()
            val password = textUserPassword.text.toString().toUpperCase()
        }

        builder.setNegativeButton("No") { dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }


}