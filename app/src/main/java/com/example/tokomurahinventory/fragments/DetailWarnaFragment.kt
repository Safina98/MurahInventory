package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.DeleteDetailWarnaClickListener
import com.example.tokomurahinventory.adapters.DetailWarnaAdapter
import com.example.tokomurahinventory.adapters.DetailWarnaClickListener
import com.example.tokomurahinventory.adapters.DetailWarnaLongListener
import com.example.tokomurahinventory.adapters.UpdateDetailWarnaClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentDetailWarnaBinding
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModel
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel


class DetailWarnaFragment : AuthFragment() {

    private lateinit var binding:FragmentDetailWarnaBinding
    private val viewModel: MerkViewModel by viewModels()
    /*
    val adapter by lazy {
        DetailWarnaAdapter(
            DetailWarnaClickListener {
                Toast.makeText(context,it.toString(),Toast.LENGTH_SHORT).show()
                //viewModel.onNavigateToDetailWarna(it.warnaRef)
            },
            DetailWarnaLongListener {
                // Handle item long click
            }, UpdateDetailWarnaClickListener {

            }, DeleteDetailWarnaClickListener {

            }
        )
    }

     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_detail_warna,container,false)
        val application = requireNotNull(this.activity).application
        var refWarna = arguments?.let { DetailWarnaFragmentArgs.fromBundle(it).refWarna}

        val dataSourceWarna = DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(application).detailWarnaDao
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = DetailWarnaViewModelFactory(dataSourceWarna,dataSourceDetailWarna,dataSourceLog,dataSourceBarangLog,refWarna!!,loggedInUser,application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(DetailWarnaViewModel::class.java)
        binding.viewModel = viewModel


        val adapter=DetailWarnaAdapter(
            DetailWarnaClickListener {

                //viewModel.onNavigateToDetailWarna(it.warnaRef)
            },
            DetailWarnaLongListener {
                // Handle item long click
            }, UpdateDetailWarnaClickListener {
                showAddDetailWarnaDialog(viewModel,it,-1)
            }, DeleteDetailWarnaClickListener {
                viewModel.deleteDetailWarna(it)
                Log.i("DETAILWARNAPROB","Adapter $it")

            }
        )

        binding.rvDetailWarna.adapter = adapter

        //Obsert detail warna recycler view
        viewModel.detailWarnaList.observe(viewLifecycleOwner, Observer {
            it.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()

            }
        })


        viewModel.addDetailWarnaFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                showAddDetailWarnaDialog(viewModel,null,0)
                viewModel.onAddWarnaFabClicked()
            }

        })


        viewModel.warna.observe(viewLifecycleOwner, Observer {
        })

        return binding.root
    }
    fun showAddDetailWarnaDialog(viewModel: DetailWarnaViewModel, detailWarnaModel: DetailWarnaModel?, i:Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Barang")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_add_warna, null)
        val textWarna = view.findViewById<EditText>(R.id.txt_warna)
        val textSatuan = view.findViewById<EditText>(R.id.txt_satuan)
        if (detailWarnaModel!=null){
            textWarna.setText(detailWarnaModel.detailWarnaPcs.toString())
            textSatuan.setText(detailWarnaModel.detailWarnaIsi.toString())
        }
        textWarna.inputType = InputType.TYPE_CLASS_NUMBER
        textSatuan.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        textWarna.setHint("pcs")
        textSatuan.setHint("isi")
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val pcs = textWarna.text.toString().toUpperCase().trim().toIntOrNull()
            val isi = textSatuan.text.toString().toUpperCase().trim().toDoubleOrNull()
            if (pcs!=null && isi!=null)
            {
                if (detailWarnaModel==null)
                {
                viewModel.insertDetailWarna(pcs,isi)
                }else{
                    //detailWarnaModel.detailWarnaPcs = pcs
                    //detailWarnaModel.detailWarnaIsi = isi
                    viewModel.updateDetailWarna(detailWarnaModel,pcs,isi)
                }

            }else
                Toast.makeText(context,"Gagal menambah data",Toast.LENGTH_SHORT).show()

        }
        builder.setNegativeButton("No") { dialog, which ->

        }
        val alert = builder.create()
        alert.show()
    }


}