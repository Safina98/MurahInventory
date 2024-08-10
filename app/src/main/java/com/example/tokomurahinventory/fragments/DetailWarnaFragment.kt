package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
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
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.CombinedViewModel
import com.example.tokomurahinventory.viewmodels.CombinedViewModelFactory
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModel
import com.google.android.material.textfield.TextInputLayout


class DetailWarnaFragment : AuthFragment() {
    private lateinit var binding:FragmentDetailWarnaBinding
    //private val viewModel: MerkViewModel by viewModels()
    private lateinit var viewModel:CombinedViewModel
    private var isDialogShowing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_detail_warna,container,false)
        val application = requireNotNull(this.activity).application
        var refWarna = arguments?.let { DetailWarnaFragmentArgs.fromBundle(it).refWarna}
        val merkDao = DatabaseInventory.getInstance(application).merkDao
        val warnaDao = DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(application).detailWarnaDao
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val refMerk =""
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?:""
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(requireActivity(), CombinedViewModelFactory(merkDao, warnaDao, refMerk, loggedInUser,dataSourceDetailWarna,dataSourceLog,dataSourceBarangLog, application)).get(
            CombinedViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val adapter=DetailWarnaAdapter(
            DetailWarnaClickListener {
                //viewModel.onNavigateToDetailWarna(it.warnaRef)
            },
            DetailWarnaLongListener {
                // Handle item long click
                DialogUtils.showCreratedEdited(requireContext(),it.createdBy ?: it.user!!,it.lastEditedBy ?: it.user!!, it.detailWarnaDate,it.detailWarnaLastEditedDate,it.detailWarnaKet)
            }, UpdateDetailWarnaClickListener {
                showAddDetailWarnaDialog(viewModel,it,-1)
            }, DeleteDetailWarnaClickListener {
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as DetailWarnaViewModel).deleteDetailWarna(item as DetailWarnaModel) })
            }
        )
        binding.rvDetailWarna.adapter = adapter
        viewModel.isDetailWarnaLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.i("LoadLogProbs", "isLoading observer : $isLoading")
            if (isLoading) {
                binding.progressBarDetail.visibility = View.VISIBLE
                binding.rvDetailWarna.visibility = View.GONE
                binding.textDetailCrashed.visibility = View.GONE
            } else {
                binding.progressBarDetail.visibility = View.GONE
                if (viewModel.isLoadDetailWarnaCrashed.value == true) {
                    binding.rvDetailWarna.visibility = View.GONE
                    binding.textDetailCrashed.visibility = View.VISIBLE
                } else {
                    binding.rvDetailWarna.visibility = View.VISIBLE
                    binding.textDetailCrashed.visibility = View.GONE
                }
            }
        }
        viewModel.isLoadDetailWarnaCrashed.observe(viewLifecycleOwner) { hasCrashed ->
            if (hasCrashed) {
                // Only show crash message if there was an actual crash
                binding.textDetailCrashed.visibility = View.VISIBLE
                binding.rvDetailWarna.visibility = View.GONE
            } else {
                // Hide the crash message if there's no crash
                binding.textDetailCrashed.visibility = View.GONE
                // RecyclerView visibility will be handled by the isLogLoading observer
            }
        }
        binding.textDetailCrashed.setOnClickListener{
            val warnaRef = viewModel.refWarna.value
            if (warnaRef != null) {
                viewModel.getDetailWarnaByWarnaRef(warnaRef)
            }
        }
        viewModel.refWarna.observe(viewLifecycleOwner) {
            Log.i("SplitFragmetProbs", "refWarna ${it}")
            it?.let {
                viewModel.getDetailWarnaByWarnaRef(it)
            }
        }
        //Obsert detail warna recycler view
        viewModel.detailWarnaList.observe(viewLifecycleOwner) {
            it.let {
                adapter.submitList(it.sortedBy { it.detailWarnaIsi })
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.addDetailWarnaFab.observe(viewLifecycleOwner) {
            if (it == true) {
                showAddDetailWarnaDialog(viewModel, null, 0)
                viewModel.onAddDetailWarnaFabClicked()
            }
        }
        return binding.root
    }
    fun showAddDetailWarnaDialog(viewModel:CombinedViewModel, detailWarnaModel: DetailWarnaModel?, i:Int){
        if (isDialogShowing) return

        isDialogShowing = true
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Barang")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pop_up_detail_warna, null)
        val textIsi = view.findViewById<EditText>(R.id.txt_isi)
        val textPcs = view.findViewById<EditText>(R.id.txt_pcs)
        val input1 = view.findViewById<TextInputLayout>(R.id.layout_satu)
        val input2 = view.findViewById<TextInputLayout>(R.id.layout_dua)
        if (detailWarnaModel!=null){
            textPcs.setText(detailWarnaModel.detailWarnaPcs.toString())
            textIsi.setText(detailWarnaModel.detailWarnaIsi.toString())
        }
        textPcs.inputType = InputType.TYPE_CLASS_NUMBER
        textIsi.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input1.setHint("isi")
        input2.setHint("pcs")
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val pcs = textPcs.text.toString().uppercase().trim().toIntOrNull()
            val isi = textIsi.text.toString().uppercase().trim().toDoubleOrNull()
            if (pcs!=null && isi!=null)
            {
                viewModel.insertDetailWarna(pcs,isi)
            }else
                Toast.makeText(context,"Gagal menambah data",Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        builder.setOnDismissListener {
            isDialogShowing = false
        }
        val alert = builder.create()
        alert.show()
    }
}