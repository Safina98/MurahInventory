package com.example.tokomurahinventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentInputStokBinding

import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModel
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModelFactory
import com.example.tokomurahinventory.viewmodels.InputStokViewModel
import com.example.tokomurahinventory.viewmodels.InputStokViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel


class InputStokFragment : AuthFragment() {

    private lateinit var binding: FragmentInputStokBinding
    private val viewModel: InputStokViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_stok,container,false)
        val application = requireNotNull(this.activity).application

        val dataSourceInputLog = DatabaseInventory.getInstance(application).inputLogDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = InputStokViewModelFactory(dataSourceInputLog,loggedInUser,application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(InputStokViewModel::class.java)
        binding.viewModel = viewModel



        return binding.root
    }
}