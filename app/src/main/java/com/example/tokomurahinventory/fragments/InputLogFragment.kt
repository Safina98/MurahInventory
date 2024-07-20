package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.AddNetClickListener
import com.example.tokomurahinventory.adapters.BarangLogIsiClickListener
import com.example.tokomurahinventory.adapters.BarangLogKodeClickListener
import com.example.tokomurahinventory.adapters.BarangLogMerkClickListener
import com.example.tokomurahinventory.adapters.BarangLogPcsClickListener
import com.example.tokomurahinventory.adapters.CountAdapter
import com.example.tokomurahinventory.adapters.DeleteNetClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentInputLogBinding
import com.example.tokomurahinventory.databinding.PopUpAutocompleteTextviewBinding
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel


class InputLogFragment : AuthFragment() {

    private lateinit var binding: FragmentInputLogBinding
    private lateinit var viewModel: LogViewModel
    private var isDialogShowing = false
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_log,container,false)

        val application = requireNotNull(this.activity).application
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourcebarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceMerk =  DatabaseInventory.getInstance(application).merkDao
        val dataSourceWarna =  DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna =  DatabaseInventory.getInstance(application).detailWarnaDao
        // val viewModelFactory = LogViewModelFactory(application)
        binding.lifecycleOwner = this
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        viewModel = ViewModelProvider(
            requireActivity(),
            LogViewModelFactory(dataSourceMerk, dataSourceWarna, dataSourceDetailWarna, dataSourceLog, dataSourcebarangLog, loggedInUser,application)
        ).get(LogViewModel::class.java)

        binding.viewModel = viewModel

        val adapter = CountAdapter(
            AddNetClickListener { countModel, position -> },
            DeleteNetClickListener { countModel, position ->
                clearEditText()
                viewModel.deleteCountModel(countModel, position) },
            BarangLogMerkClickListener { countModel, position ->
                clearEditText()
                showPopUpDialog(countModel, position, "Merk") },
            BarangLogKodeClickListener { countModel, position ->
                clearEditText()
                if (countModel.merkBarang!=null){
                    isDialogShowing = true
                    viewModel.getWarnaByMerk(countModel.merkBarang!!)
                    viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { it ->
                        if(it!=null){
                            showPopUpDialog(countModel, position, "Warna")
                        }
                    }
                }else Toast.makeText(application,"Masukkan Merk",Toast.LENGTH_SHORT).show()

            }, BarangLogIsiClickListener{countModel, position ->
                clearEditText()
                if (countModel.merkBarang!=null && countModel.kodeBarang!=null) {
                    viewModel.getIsiByWarnaAndMerk(countModel.merkBarang!!,countModel.kodeBarang!!)
                    viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner) { it ->
                        if(it!=null){
                            showPopUpDialog(countModel, position, "Isi")
                        }
                    }
                }else Toast.makeText(application,"Masukkan Merk dan warna",Toast.LENGTH_SHORT).show()
            },
            BarangLogPcsClickListener{countModel, position ->
                clearEditText()
                showPopUpDialog(countModel, position, "Pcs")
            },
            viewModel, this
        )


        binding.rvAddBarang.adapter = adapter
        viewModel.countModelList.observe(viewLifecycleOwner) { it?.let {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }}
        viewModel.allMerkFromDb.observe(viewLifecycleOwner){it?.let {}}
        viewModel.navigateToLog.observe(viewLifecycleOwner, Observer {
            if (it==true){
                this.findNavController().navigate(InputLogFragmentDirections.actionInputLogFragmentToLogFragment())
                viewModel.onNavigatedToLog()
            }
        })

        return binding.root
    }
    fun showPopUpDialog(countModel: CountModel?, position: Int, code: String) {
        // Dismiss any existing dialog to ensure only one dialog is shown at a time
        dialog?.dismiss()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle( when (code) {
            "Merk" -> "Merk"
            "Warna" -> "Kode Warna"
            "Isi" -> "Isi"
            else -> "Pcs"
        })

        val binding: PopUpAutocompleteTextviewBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.pop_up_autocomplete_textview, null, false)
        binding.viewModel = viewModel
        val autoCompleteTextView: AutoCompleteTextView = binding.autocompleteText

        // Convert the list of Double to a list of String and then to an array
        val suggestions = when (code) {
            "Merk" -> viewModel.allMerkFromDb.value?.toTypedArray() ?: arrayOf()
            "Warna" -> viewModel.codeWarnaByMerk.value?.toTypedArray() ?: arrayOf()
            "Isi" -> viewModel.isiByWarnaAndMerk.value?.map { it.toString() }?.toTypedArray() ?: arrayOf()
            else -> arrayOf()
        }

        if (suggestions.isEmpty()) {
            Log.e("showPopUpDialog", "Suggestions for $code are empty")
        }

        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        autoCompleteTextView.setAdapter(adapter)

        // Show dropdown when AutoCompleteTextView is clicked
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }

        // Show dropdown when AutoCompleteTextView is focused
        autoCompleteTextView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
            }
        }
        when (code) {
            "Isi", "Pcs" -> {
                autoCompleteTextView.inputType = InputType.TYPE_CLASS_NUMBER

            }
            else -> {
                autoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT
            }
        }

        // Set the custom view to the AlertDialog
        builder.setView(binding.root)

        builder.setPositiveButton("OK") { dialog, which ->
            // Handle the positive button click
            var name  = autoCompleteTextView.text.toString()
            when (code){
                "Merk" -> {
                    viewModel.updateMerk(position,name)
                }
                "Warna" -> {
                    viewModel.updateKode(position,name)
                }
                "Isi" -> {
                    // Convert the list of Double to a list of String and then to an array
                    viewModel.updateIsi(position,name.toDouble())
                }
                "Pcs" -> {
                    // Conve    rt the list of Double to a list of String and then to an array
                    viewModel.updatePcs(position,name.toInt())
                }
            }
            viewModel.setLiveDataToNull()
            dialog.dismiss()  // Dismiss the dialog after handling input
        }

        builder.setNegativeButton("No") { dialog, which ->
            // Handle the negative button click
            viewModel.setLiveDataToNull()
            dialog.dismiss()  // Dismiss the dialog after handling input
        }

        dialog = builder.create()
        dialog?.show()
    }

    fun clearEditText(){
        binding.inputPembeli.clearFocus()
        binding.inputKet.clearFocus()
    }



}