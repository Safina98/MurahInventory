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
import com.example.tokomurahinventory.viewmodels.WarnaViewModel
import com.example.tokomurahinventory.viewmodels.WarnaViewModelFactory
import com.google.android.material.textfield.TextInputLayout

class WarnaFragment : AuthFragment() {

    private lateinit var binding: FragmentWarnaBinding
    private lateinit var viewModel: WarnaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewModel
        Log.i("FRAGMENT LIFECYCLE", "onCreate called")
        val application = requireNotNull(this.activity).application
        val refMerk = arguments?.let { WarnaFragmentArgs.fromBundle(it).refMerk }
        if (refMerk == null) {
            Log.e("FRAGMENT LIFECYCLE", "refMerk is null in onCreate")
            return
        }
        val dataSourceWarna = DatabaseInventory.getInstance(application).warnaDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext())
        if (loggedInUser == null) {
            Log.e("FRAGMENT LIFECYCLE", "loggedInUser is null in onCreate")
            return
        }

        val viewModelFactory = WarnaViewModelFactory(dataSourceWarna, refMerk, loggedInUser, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(WarnaViewModel::class.java)
        viewModel.getWarnaByMerk()

        Log.i("FRAGMENT LIFECYCLE", "ViewModel initialized in onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("FRAGMENT LIFECYCLE", "onCreateView called")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_warna, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.getWarnaByMerk()
        val adapter = WarnaAdapter(
            WarnaClickListener {
                Log.i("WarnaProb", "warna table : $it")
                viewModel.onNavigateToDetailWarna(it.warnaRef)
            },
            WarnaLongListener {
                // Handle item long click
            },
            UpdateWarnaClickListener {
                showAddWarnaDialog(viewModel, it, 1)
            },
            DeleteWarnaClickListener {
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as WarnaViewModel).deleteWarna(item as WarnaModel) })
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
                viewModel.onNavigatetedToDetailWarna()
                viewModel.clearScope()
            }
        })

        return binding.root
    }

    private fun showAddWarnaDialog(viewModel: WarnaViewModel, warnaTable: WarnaModel?, i: Int) {
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
            val kodeWarna = textWarna.text.toString().uppercase()
            val kodeSatuan = textSatuan.text.toString().uppercase()
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
        viewModel.getWarnaByMerk()
        Log.i("FRAGMENT LIFECYCLE", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.i("FRAGMENT LIFECYCLE", "onResume called")
        viewModel.getWarnaByMerk()
    }
}
