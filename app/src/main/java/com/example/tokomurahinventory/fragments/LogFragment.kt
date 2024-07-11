package com.example.tokomurahinventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.LogAdapter
import com.example.tokomurahinventory.adapters.LogClickListener
import com.example.tokomurahinventory.adapters.LogDeleteListener
import com.example.tokomurahinventory.adapters.LogLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentLogBinding
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory


class LogFragment : AuthFragment(){
    private lateinit var binding: FragmentLogBinding
    private lateinit var viewModel: LogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_log,container,false)

        val application = requireNotNull(this.activity).application
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourcebarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceMerk =  DatabaseInventory.getInstance(application).merkDao
        val dataSourceWarna =  DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna =  DatabaseInventory.getInstance(application).detailWarnaDao
       // val viewModelFactory = LogViewModelFactory(application)
        binding.lifecycleOwner =this
        //val viewModel = ViewModelProvider(this,viewModelFactory).get(LogViewModel::class.java)
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        viewModel = ViewModelProvider(requireActivity(), LogViewModelFactory(dataSourceMerk,dataSourceWarna,dataSourceDetailWarna,dataSourceLog,dataSourcebarangLog,loggedInUser,application))
            .get(LogViewModel::class.java)
        binding.viewModel = viewModel


        viewModel.resetTwoWayBindingSub()
        val adapter  = LogAdapter(
            LogClickListener {
               // viewModel.onNavigateToWarna(it.refMerk)
                viewModel.populateMutableLiveData(it)
                viewModel.onAddLogFabClick()
            },
            LogLongListener {
                // Handle item long click
            }, LogDeleteListener {
                viewModel.deleteLog(it)
            }
        )

        binding.rvLog.adapter = adapter

        //adapter.submitList(viewModel.logDummy)
        viewModel.allLog.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })
        binding.searchBarLog.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterLog(newText)
                return true
            }
        })

        viewModel.addLogFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                this.findNavController().navigate(LogFragmentDirections.actionLogFragmentToInputLogFragment())
                viewModel.onAddLogFabClicked()
            }
        })

        return binding.root
    }
}