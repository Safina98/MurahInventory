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
import com.example.tokomurahinventory.adapters.DeleteWarnaClickListener
import com.example.tokomurahinventory.adapters.UpdateWarnaClickListener
import com.example.tokomurahinventory.adapters.WarnaAdapter
import com.example.tokomurahinventory.adapters.WarnaClickListener
import com.example.tokomurahinventory.adapters.WarnaLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentWarnaBinding
import com.example.tokomurahinventory.models.model.WarnaModel
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.WarnaViewModel
import com.example.tokomurahinventory.viewmodels.WarnaViewModelFactory


class WarnaFragment : AuthFragment() {
    private lateinit var binding : FragmentWarnaBinding
    private val viewModel: WarnaViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_warna,container,false)
        val application = requireNotNull(this.activity).application
        var refMerk = arguments?.let { WarnaFragmentArgs.fromBundle(it).refMerk}
        val dataSourceWarna = DatabaseInventory.getInstance(application).warnaDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext())
        val viewModelFactory = WarnaViewModelFactory(dataSourceWarna,refMerk!!,loggedInUser!!,application)
        binding.lifecycleOwner =this

        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(WarnaViewModel::class.java)
        binding.viewModel = viewModel


        val adapter  = WarnaAdapter(
            WarnaClickListener {
                Log.i("WarnaProb","warna table : $it")
                viewModel.onNavigateToDetailWarna(it.warnaRef)

            },
            WarnaLongListener {
                // Handle item long click
            },
            UpdateWarnaClickListener {
                showAddWarnaDialog(viewModel,it,1)
            },
            DeleteWarnaClickListener {
                viewModel.deleteWarna(it)
            }
        )
        binding.rvWarna.adapter = adapter
        viewModel.allWarnaByMerk.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()

            }
        })
        viewModel.addWanraFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                showAddWarnaDialog(viewModel,null,0)
                viewModel.onAddWarnaFabClicked()
            }
        })
        binding.searchBarWarna.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterWarna(newText)
                return true
            }
        })
        viewModel.navigateToDetailWarna.observe(viewLifecycleOwner, Observer {
            if (it!=null){
                this.findNavController().navigate(WarnaFragmentDirections.actionWarnaFragmentToDetailWarnaFragment(it))
                viewModel.onNavigatetedToDetailWarna()
            }
        })
        return binding.root
    }
    fun showAddWarnaDialog(viewModel: WarnaViewModel, warnaTable: WarnaModel?, i:Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Warna")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_add_warna, null)
        val textWarna = view.findViewById<EditText>(R.id.txt_warna)
        val textSatuan = view.findViewById<EditText>(R.id.txt_satuan)
        if (warnaTable!=null){
            textWarna.setText(warnaTable.kodeWarna)
            textSatuan.setText(warnaTable.satuan)
        }
        textWarna.setHint("Kode warna")
        textSatuan.setHint("Satuan (meter/yard/dll)")
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val kodeWarna = textWarna.text.toString().toUpperCase()
            val kodeSatuan = textSatuan.text.toString().toUpperCase()
            if (warnaTable==null){
                viewModel.insertWarna(kodeWarna,kodeSatuan)
            }else {
                warnaTable.kodeWarna = kodeWarna
                warnaTable.satuan = kodeSatuan
                viewModel.updateWarna(warnaTable)
            }
        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        val alert = builder.create()
        alert.show()
    }
}