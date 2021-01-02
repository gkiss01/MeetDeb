package com.gkiss01.meetdeb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.gkiss01.meetdeb.data.remote.response.User
import com.gkiss01.meetdeb.screens.flow.StartFlowFragmentDirections
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModelKoin: ActivityViewModel by viewModel()
    private val appNavController: NavController by lazy { findNavController(R.id.app_nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun changeNavGraphToStart() {
        appNavController.navigateUp()
    }

    fun changeNavGraphToMain() {
        appNavController.navigate(StartFlowFragmentDirections.actionStartFlowFragmentToMainFlowFragment())
    }

    override fun onSupportNavigateUp(): Boolean {
        val currentFlowFragmentId = appNavController.currentDestination?.id
        if (currentFlowFragmentId == R.id.mainFlowFragment) {
            val mainNavController = findNavController(R.id.main_nav_host_fragment)
            return mainNavController.navigateUp() || super.onSupportNavigateUp()
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
