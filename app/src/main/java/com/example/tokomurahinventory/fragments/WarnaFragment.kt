package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
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
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.CombinedViewModel
import com.example.tokomurahinventory.viewmodels.CombinedViewModelFactory
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import com.example.tokomurahinventory.viewmodels.WarnaViewModel
import com.example.tokomurahinventory.viewmodels.WarnaViewModelFactory
import com.google.android.material.textfield.TextInputLayout

class WarnaFragment : AuthFragment() {

    private lateinit var binding: FragmentWarnaBinding
    //private lateinit var viewModel: WarnaViewModel
    private lateinit var viewModel: CombinedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewModel

        Log.i("FRAGMENT LIFECYCLE", "ViewModel initialized in onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("FRAGMENT LIFECYCLE", "onCreateView called")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_warna, container, false)
        Log.i("FRAGMENT LIFECYCLE", "onCreate called")
        val application = requireNotNull(this.activity).application
        val merkDao = DatabaseInventory.getInstance(application).merkDao
        val warnaDao = DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(application).detailWarnaDao
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val refMerk =""
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?:""
        binding.lifecycleOwner = this
        //val factory = CombinedViewModelFactory(merkDao, warnaDao, refMerk, loggedInUser, requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), CombinedViewModelFactory(merkDao, warnaDao, refMerk, loggedInUser,dataSourceDetailWarna,dataSourceLog,dataSourceBarangLog, application)).get(
            CombinedViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //viewModel = ViewModelProvider(this, factory).get(CombinedViewModel::class.java)
        /*
        val application = requireNotNull(this.activity).application
        val refMerk = arguments?.let { WarnaFragmentArgs.fromBundle(it).refMerk }

        val dataSourceWarna = DatabaseInventory.getInstance(application).warnaDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext())

        val viewModelFactory = WarnaViewModelFactory(dataSourceWarna, refMerk?: "", loggedInUser?:"", application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(WarnaViewModel::class.java)
        viewModel.getWarnaByMerk()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

         */

        val adapter = WarnaAdapter(
            WarnaClickListener {
               // Log.i("WarnaProb", "warna table : $it")
                //viewModel.onNavigateToDetailWarna(it.warnaRef)
                viewModel.setRefWarna(it.warnaRef)
                viewModel.getStringWarna(it.warnaRef)
                viewModel.getDetailWarnaByWarnaRef(it.warnaRef)
            },
            WarnaLongListener {
                // Handle item long click
                DialogUtils.showCreratedEdited(requireContext(),it.createdBy ?:"user has been deleted",it.lastEditedBy ?: "user has been deleted", it.warnaCreatedDate,it.warnaLastEditedDate)
            },
            UpdateWarnaClickListener {
                showAddWarnaDialog(viewModel, it, 1)
            },
            DeleteWarnaClickListener {
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as CombinedViewModel).deleteWarna(item as WarnaModel) })
            }
        )

        binding.rvWarna.adapter = adapter

        viewModel.merk.observe(viewLifecycleOwner, Observer {
            Log.i("SplitFragmetProbs","merk ${it}")
        })

        viewModel.allWarnaByMerk.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it.sortedBy { it.kodeWarna })
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.refMerkk.observe(viewLifecycleOwner, Observer {
           // Log.i("SplitFragmetProbs","refMerkk ${it}")
            it?.let {
                viewModel.getWarnaByMerk(it)
            }
        })
        viewModel.refWarna.observe(viewLifecycleOwner, Observer {})
        viewModel.warna.observe(viewLifecycleOwner, Observer {
            Log.i("SplitFragmetProbs","warna ${it}")
        })

        viewModel.addWarnaFab.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                showAddWarnaDialog(viewModel, null, 0)
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
            if (it != null) {
                this.findNavController().navigate(WarnaFragmentDirections.actionWarnaFragmentToDetailWarnaFragment(it))
                //viewModel.onNavigatetedToDetailWarna()
                //viewModel.clearScope()
            }
        })

        return binding.root
    }

    private fun showAddWarnaDialog(viewModel: CombinedViewModel, warnaTable: WarnaModel?, i: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Warna")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_add_warna, null)
        val textWarna = view.findViewById<EditText>(R.id.txt_warna)
        val textSatuan = view.findViewById<AutoCompleteTextView>(R.id.txt_satuan)
        val input1 = view.findViewById<TextInputLayout>(R.id.layout_satu)
        val input2 = view.findViewById<TextInputLayout>(R.id.layout_dua)
        if (warnaTable != null) {
            textWarna.setText(warnaTable.kodeWarna)
            textSatuan.setText(warnaTable.satuan)
        }

        input1.setHint("Kode warna")
        input2.setHint("Satuan (meter/yard/dll)")
        builder.setView(view)
        val suggestions =resources.getStringArray(R.array.satuan)
        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        textSatuan.setAdapter(adapter)
        textSatuan.setOnClickListener {
            textSatuan.showDropDown()
        }

        // Show dropdown when AutoCompleteTextView is focused
        textSatuan.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                textSatuan.showDropDown()
            }
        }
        builder.setPositiveButton("OK") { dialog, which ->
            val kodeWarna = textWarna.text.toString()
            val kodeSatuan = textSatuan.text.toString()
            if (warnaTable == null) {
                viewModel.insertWarna(kodeWarna, kodeSatuan)
            } else {
                warnaTable.kodeWarna = kodeWarna
                warnaTable.satuan = kodeSatuan
                viewModel.updateWarna(warnaTable)
            }
        }

        builder.setNegativeButton("No") { dialog, which -> }

        val alert = builder.create()
        alert.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        Log.i("FRAGMENT LIFECYCLE", "onAttach called")
    }

    override fun onStart() {
        super.onStart()
        //viewModel.getWarnaByMerk()
        Log.i("FRAGMENT LIFECYCLE", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.i("FRAGMENT LIFECYCLE", "onResume called")
        //viewModel.getWarnaByMerk()
    }
}
