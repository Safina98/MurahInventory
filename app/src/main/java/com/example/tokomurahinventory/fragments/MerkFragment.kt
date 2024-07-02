package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.MerkAdapter
import com.example.tokomurahinventory.adapters.MerkClickListener
import com.example.tokomurahinventory.adapters.MerkLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentMerkBinding
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import com.example.tokomurahinventory.viewmodels.MerkViewModelFactory
import com.google.android.material.textfield.TextInputEditText


class MerkFragment : Fragment() {
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
        val viewModelFactory = MerkViewModelFactory(dataSource1,application)
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
                }
            )

        binding.rvMerk.adapter = adapter

        //Observe all merk from db
        viewModel.allMerkTable.observe(viewLifecycleOwner, Observer {
            it.let {
                adapter.submitList(it)
            }
        })
        //Observe fab merk state
        viewModel.addMerkFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                showAddDialog(viewModel,0)
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

    fun showAddDialog(viewModel: MerkViewModel,i:Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Merk Barang")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_add_item, null)
        val textBrand = view.findViewById<EditText>(R.id.txt_merk)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val merk = textBrand.text.toString().toUpperCase()
            viewModel.insertMerk(merk)
        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }


}