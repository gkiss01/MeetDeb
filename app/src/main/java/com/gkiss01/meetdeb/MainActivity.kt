package com.gkiss01.meetdeb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.screens.flow.MainFlowFragmentDirections
import com.gkiss01.meetdeb.screens.flow.StartFlowFragmentDirections
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModelKoin: ActivityViewModel by viewModel()
    private val navController: NavController by lazy { findNavController(R.id.app_nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun changeNavGraphToStart() {
        navController.navigate(MainFlowFragmentDirections.actionMainFlowFragmentToStartFlowFragment())
    }

    fun changeNavGraphToMain() {
        navController.navigate(StartFlowFragmentDirections.actionStartFlowFragmentToMainFlowFragment())
    }

    override fun onSupportNavigateUp(): Boolean {
        val currentFlowFragmentId = navController.currentDestination?.id
        if (currentFlowFragmentId == R.id.mainFlowFragment) {
            val navController = findNavController(R.id.main_nav_host_fragment)
            return navController.navigateUp() || super.onSupportNavigateUp()
        }

        return super.onSupportNavigateUp()
    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun getActiveUser(): User? = viewModelKoin.activeUser.value?.data
}
