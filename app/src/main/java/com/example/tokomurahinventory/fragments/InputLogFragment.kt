package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.example.tokomurahinventory.databinding.PopUpAddBarangLogBinding
import com.example.tokomurahinventory.databinding.PopUpAutocompleteTextviewBinding
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory


class InputLogFragment : AuthFragment() {

    private lateinit var binding: FragmentInputLogBinding
    private lateinit var viewModel: LogViewModel
    private var isDialogShowing = false
    private var dialog: AlertDialog? = null
    private lateinit var adapter: CountAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

         adapter = CountAdapter(
            AddNetClickListener { countModel, position -> },
            DeleteNetClickListener { countModel, position ->
                clearEditText()
                viewModel.deleteCountModel(countModel.id)
                notifyAnItemDeleted(position)
                                   },
            BarangLogMerkClickListener { countModel, position ->
                clearEditText()
                //setupDialog(countModel)
                showPopUpDialog(countModel.id, "Merk")
                },
            BarangLogKodeClickListener { countModel, position ->
                clearEditText()

                if (countModel.merkBarang!=null){
                    //setupDialog(countModel)
                    isDialogShowing = true
                    viewModel.getWarnaByMerk(countModel.merkBarang!!)
                    viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { it ->
                        if(it!=null){
                            showPopUpDialog(countModel.id, "Warna")
                        }
                    }
                }else Toast.makeText(application,"Masukkan Merk",Toast.LENGTH_SHORT).show()

            }, BarangLogIsiClickListener{countModel, position ->
                clearEditText()
                if (countModel.merkBarang!=null && countModel.kodeBarang!=null) {
                    viewModel.getIsiByWarnaAndMerk(countModel.merkBarang!!,countModel.kodeBarang!!)
                    viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner) { it ->
                        if(it!=null){
                            showPopUpDialog(countModel.id, "Isi")
                        }
                    }
                }else Toast.makeText(application,"Masukkan Merk dan warna",Toast.LENGTH_SHORT).show()
            },
            BarangLogPcsClickListener{countModel, position ->
                clearEditText()
                if (countModel.isi!=null) showPopUpDialog(countModel.id, "Pcs")
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

    private fun setupDialog(countModel: CountModel?) {
        val dialogBinding = DataBindingUtil.inflate<PopUpAddBarangLogBinding>(
            LayoutInflater.from(context),
            R.layout.pop_up_add_barang_log,
            null,
            false
        )

        val autoCompleteMerk = dialogBinding.txtMerk
        val autoCompleteWarna = dialogBinding.txtWarna
        val autoCompleteIsi = dialogBinding.txtIsi
        val etPcs = dialogBinding.txtPcs

        // Use mutable lists for adapters
        val merkAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteMerk.setAdapter(merkAdapter)

        val warnaAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteWarna.setAdapter(warnaAdapter)

        val isiAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteIsi.setAdapter(isiAdapter)

        if (countModel != null) {
            autoCompleteMerk.setText(countModel.merkBarang)
            autoCompleteWarna.setText(countModel.kodeBarang)
            if (countModel.isi!=null)autoCompleteIsi.setText(countModel.isi.toString())
            etPcs.setText(countModel.psc.toString())
        }

        // Fetch and observe data
        viewModel.allMerkFromDb.observe(viewLifecycleOwner) { allMerk ->
            merkAdapter.clear()
            merkAdapter.addAll(allMerk)
        }

        autoCompleteMerk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val merk = s.toString()
                if (merk.isNotEmpty()) {
                    viewModel.getWarnaByMerk(merk)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { warnaList ->
            warnaAdapter.clear()
            warnaAdapter.addAll(warnaList ?: emptyList())
        }

        autoCompleteWarna.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val warna = s.toString()
                val merk = autoCompleteMerk.text.toString()
                if (warna.isNotEmpty() && merk.isNotEmpty()) {
                    viewModel.getIsiByWarnaAndMerk(merk, warna)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner) { isiList ->
            isiAdapter.clear()
            isiAdapter.addAll(isiList?.map { it.toString() } ?: emptyList())
        }

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null) // No action here
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                if (countModel != null) {
                    countModel.merkBarang = autoCompleteMerk.text.toString().trim()
                    countModel.kodeBarang = autoCompleteWarna.text.toString().trim()
                    if (countModel.isi!=null) countModel.isi = autoCompleteIsi.text.toString().trim().toDouble()
                    countModel.psc = etPcs.text.toString().trim().toInt()
                    Log.i("InsertLogTry", "pop up dialog countModel: $countModel")

                    // Handle dialog dismissal based on update result
                    viewModel.updateCountModel(countModel) { success ->
                        if (success) {
                            dialog.dismiss() // Dismiss only if update is successful
                        } else {
                            Toast.makeText(requireContext(), "Update failed, try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


    fun showPopUpDialog(position: Int, code: String) {
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
    private fun notifyAnItemDeleted(position: Int) {
            adapter.notifyItemRemoved(position)
    }


}