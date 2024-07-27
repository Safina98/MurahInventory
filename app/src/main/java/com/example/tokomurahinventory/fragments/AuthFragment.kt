package com.example.tokomurahinventory.fragments



import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tokomurahinventory.viewmodels.AuthViewModel

abstract class AuthFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.authenticationState.observe(this) { isAuthenticated ->
            if (isAuthenticated==null || isAuthenticated==false) {
                Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show()
            }
        }
        //checkAuthentication()
    }
}
