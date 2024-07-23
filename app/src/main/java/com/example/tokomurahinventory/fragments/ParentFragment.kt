package com.example.tokomurahinventory.fragments

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.databinding.FragmentParentBinding


class ParentFragment : Fragment() {

    private lateinit var binding: FragmentParentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val fragmentLeft = MerkFragment()
            val fragmentRight = WarnaFragment()

            fragmentTransaction.add(R.id.fragment_container_left, fragmentLeft)
            fragmentTransaction.add(R.id.fragment_container_right, fragmentRight)
        } else {
            // Uncomment and modify if you want to handle portrait orientation
            // val fragmentSingle = MerkFragment()
            // fragmentTransaction.add(R.id.fragment_container, fragmentSingle)
        }

        fragmentTransaction.commit()
    }

}