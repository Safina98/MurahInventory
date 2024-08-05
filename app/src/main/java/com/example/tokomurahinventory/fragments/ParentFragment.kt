package com.example.tokomurahinventory.fragments

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tokomurahinventory.R
import androidx.fragment.app.commit
import com.example.tokomurahinventory.MainActivity

class ParentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = R.layout.fragment_parent_landscape
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load fragments based on orientation
        loadFragments()
    }



    private fun loadFragments() {
        val orientation = resources.configuration.orientation
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_left, MerkFragment())
        transaction.replace(R.id.fragment_container_center, WarnaFragment())
        transaction.replace(R.id.fragment_container_right, DetailWarnaFragment())
        transaction.commit()
    }
}
