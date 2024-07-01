package com.example.tokomurahinventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.MerkAdapter
import com.example.tokomurahinventory.adapters.MerkClickListener
import com.example.tokomurahinventory.adapters.MerkLongListener
import com.example.tokomurahinventory.databinding.FragmentMerkBinding
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.viewmodels.MerkViewModel


class MerkFragment : Fragment() {
    private lateinit var binding: FragmentMerkBinding
    private val viewModel:MerkViewModel by viewModels()
    private val adapter by lazy {
        MerkAdapter(
            MerkClickListener {
                // Handle item click
            },
            MerkLongListener {
                // Handle item long click
            }
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_merk,container,false)


        binding.rvMerk.adapter = adapter
        var listDummyMerk= mutableListOf<MerkTable>()
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkTable(2,"carrera","sdfas"))
        listDummyMerk.add(MerkTable(3,"fisesta","sdfas"))
        adapter.submitList(listDummyMerk)
        return binding.root
    }


}