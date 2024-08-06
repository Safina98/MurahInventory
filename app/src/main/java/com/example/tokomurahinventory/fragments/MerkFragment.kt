package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
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
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.DraggableFloatingActionButton
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.formatDateToString
import com.example.tokomurahinventory.utils.viewerAndEditorNotAuthorized
import com.example.tokomurahinventory.utils.viewerNotAuthorized
import com.example.tokomurahinventory.viewmodels.CombinedViewModel
import com.example.tokomurahinventory.viewmodels.CombinedViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import com.example.tokomurahinventory.viewmodels.MerkViewModelFactory
import com.example.tokomurahinventory.viewmodels.UserAction
import java.util.Date


class MerkFragment : AuthFragment() {
    private lateinit var binding: FragmentMerkBinding
    private lateinit var viewModel: CombinedViewModel
   // private val viewModel:MerkViewModel by viewModels()
//   private val viewModel:CombinedViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_merk,container,false)

        val application = requireNotNull(this.activity).application
        val merkDao = DatabaseInventory.getInstance(application).merkDao
        val warnaDao = DatabaseInventory.getInstance(application).warnaDao
       val dataSourceDetailWarna = DatabaseInventory.getInstance(application).detailWarnaDao
       val dataSourceLog = DatabaseInventory.getInstance(application).logDao
       val dataSourceBarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val refMerk =""
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?:""

       // val factory = CombinedViewModelFactory(merkDao, warnaDao, refMerk, loggedInUser, requireActivity().application)
       viewModel = ViewModelProvider(requireActivity(), CombinedViewModelFactory(merkDao, warnaDao, refMerk, loggedInUser, dataSourceDetailWarna,dataSourceLog,dataSourceBarangLog,requireActivity().application)).get(
            CombinedViewModel::class.java)

        //val viewModel = ViewModelProvider(this, factory).get(CombinedViewModel::class.java)

        /*
                val viewModelFactory = MerkViewModelFactory(dataSource1,loggedInUser,application)
                binding.lifecycleOwner =this
                val viewModel = ViewModelProvider(this,viewModelFactory)
                    .get(MerkViewModel::class.java)
                binding.viewModel = viewModel

                 */
       binding.viewModel = viewModel
       binding.lifecycleOwner = viewLifecycleOwner
        val adapter  = MerkAdapter(
            MerkClickListener {
                viewModel.setRefMerk(it.refMerk)
                viewModel.getWarnaByMerk(it.refMerk)
                viewModel.getStringMerk(it.refMerk)
                viewModel.setRefWarna("")
                viewModel.getStringWarna("")
               // viewModel.onNavigateToWarna(it.refMerk)
                },
            MerkLongListener {
                    // Handle item long click
                            DialogUtils.showCreratedEdited(requireContext(),it.createdBy ?: it.user!!,it.lastEditedBy ?: it.user!!, it.merkCreatedDate,it.merkLastEditedDate)
                },
            UpdateMerkClickListener{
                showAddDialog(viewModel,it,1)

            },
            DeleteMerkClickListener{
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as CombinedViewModel).deleteMerk(item as MerkTable) })
            }
            )

        binding.rvMerk.adapter = adapter

        //showLoginDialog()
       viewModel.refMerkk.observe(viewLifecycleOwner, Observer {
           // Log.i("SplitFragmetProbs","refMerkk ${it}")
       })
        //Observe all merk from db
        viewModel.allMerkTable.observe(viewLifecycleOwner, Observer {
            it.let {
                adapter.submitList(it.sortedBy { it.namaMerk })
                adapter.notifyDataSetChanged()
            }
        })
       viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
           Log.i("LoadLogProbs", "isLoading observer : $isLoading")
           if (isLoading) {
               binding.progressBarMerk.visibility = View.VISIBLE
               binding.rvMerk.visibility = View.GONE
               binding.textMerkCrashed.visibility = View.GONE
           } else {
               // This should only hide the ProgressBar if not loading
               binding.progressBarMerk.visibility = View.GONE
               // Check if the data loading was successful or not
               if (viewModel.isLoadMerkCrashed.value == true) {
                   binding.rvMerk.visibility = View.GONE
                   binding.textMerkCrashed.visibility = View.VISIBLE
               } else {
                   binding.rvMerk.visibility = View.VISIBLE
                   binding.textMerkCrashed.visibility = View.GONE
               }
           }
       }

       viewModel.isLoadMerkCrashed.observe(viewLifecycleOwner) { hasCrashed ->
           if (hasCrashed) {
               // Only show crash message if there was an actual crash
               binding.textMerkCrashed.visibility = View.VISIBLE
               binding.rvMerk.visibility = View.GONE
           } else {
               // Hide the crash message if there's no crash
               binding.textMerkCrashed.visibility = View.GONE
               // RecyclerView visibility will be handled by the isLogLoading observer
           }
       }
       binding.textMerkCrashed.setOnClickListener{
           viewModel.getAllMerkTable()
       }

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
       viewModel.navigateToWarna.observe(viewLifecycleOwner, Observer { shouldNavigate ->
           if (shouldNavigate!=null) {
               Log.d(":ParentFragment", "merk fragment shoundNavigate $shouldNavigate")
               this.findNavController().navigate(MerkFragmentDirections.actionMerkFragmentToWarnaFragment(shouldNavigate))
               viewModel.onNavigatedToWarna() // Reset the navigation state
               try {

               }catch (e:Exception){
                   Log.e("ParentFragment","$e")
               }

           }
       })

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(":SplitFragmetProbs", "View Created")
        // Optionally find and log the button instance
        val button: DraggableFloatingActionButton = view.findViewById(R.id.btn_add_new_merk)
        Log.d(":SplitFragmetProbs", "Button: $button")
    }
    fun showAddDialog(viewModel: CombinedViewModel, merkTable:MerkTable?, i:Int){
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


    override fun onStart() {
        super.onStart()
        viewModel.getAllMerkTable()
    }
}