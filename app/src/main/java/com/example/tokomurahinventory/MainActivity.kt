package com.example.tokomurahinventory


import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.tokomurahinventory.databinding.ActivityMainBinding
import com.example.tokomurahinventory.fragments.ParentFragment
import com.example.tokomurahinventory.utils.AppLifecycleObserver
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.AuthViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private val authViewModel: AuthViewModel by viewModels()
    private var dialog: AlertDialog? = null
    private lateinit var appLifecycleObserver: AppLifecycleObserver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)
        appLifecycleObserver = AppLifecycleObserver(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        // Check if the table is empty and insert a default user if needed
        authViewModel.checkAndInsertDefaultUser(applicationContext)
        checkAuthentication()
        authViewModel.showLoginDialog.observe(this) { shouldShow ->
            if (shouldShow) {
                showLoginDialog()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Log.d("AppDebug", "Logout action triggered.")
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkAuthentication() {
        authViewModel.authenticationState.observe(this) { isAuthenticated ->
            if (isAuthenticated != null) {
                Log.d("AppDebug", "Check authentication isAuthencicated. $isAuthenticated.")
                if (isAuthenticated) {

                    Log.d("AppDebug", "Check authentication Login successful.")
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    dialog?.dismiss() // Dismiss the dialog if it's showing
                } else {
                    Log.d("AppDebug", "Check authentication Login failed. Invalid username or password.")
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("AppDebug", "Check authentication isAuthenticaded ==null, show login dialog.")
                showLoginDialog()
            }
        }
    }

    private fun showLoginDialog() {
        if (isFinishing || isDestroyed) {
            Log.d("AppDebug", "Activity is finishing or destroyed. Not showing login dialog.")
            return
        }
        Log.d("AppDebug", "Showing login dialog.")
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_login, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnLogin = dialogView.findViewById<Button>(R.id.btnLogin)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_role)
        spinner.visibility = View.GONE
        dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            Log.d("AppDebug", "Attempting login with username: $username")
            if (username.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.authenticate(username, password, applicationContext) { isAuthenticated ->
                    if (isAuthenticated) {
                        Log.d("AppDebug", "Login successful.")
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        dialog!!.dismiss() // Dismiss the dialog after successful login
                    } else {
                        Log.d("AppDebug", "Login failed. Invalid username or password.")
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("AppDebug", "Username or password is empty.")
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        dialog!!.show()
    }




    override fun onResume() {
        super.onResume()
        //Log.d("AppDebug", "Activity resumed. Checking authentication.")

    }

    fun logout() {
        Log.d("AppDebug", "Logout action triggered.")
        // Navigate to start destination and clear back stack
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.popBackStack(navController.graph.startDestinationId, false)
        navController.navigate(navController.graph.startDestinationId)

        SharedPreferencesHelper.clearUsername(this)
        authViewModel.setAuthenticationState(null)

    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Find the ParentFragment and call updateUI
        Log.d("ParentFragment", "activity configuration change")
        val parentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? ParentFragment
        parentFragment?.updateUI()
    }


    override fun onDestroy() {
        super.onDestroy()
        /*
        if (this::dialog!!.isInitialized && dialog!!.isShowing) {
            Log.d("AppDebug", "Dismiss dialog in onDestroy.")
            dialog!!.dismiss()
        }

         */
    }
}
