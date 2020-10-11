package com.gkiss01.meetdeb.screens.flow

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.utils.mainActivity
import kotlinx.android.synthetic.main.fragment_flow_main.*

class MainFlowFragment : Fragment(R.layout.fragment_flow_main) {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        childFragmentManager.findFragmentById(R.id.main_nav_host_fragment)?.let {
            val navController = it.findNavController()
            main_navigationView.setupWithNavController(navController)
            main_navigationView.setOnNavigationItemReselectedListener {  }

            mainActivity?.apply {
                val appBarConfiguration = AppBarConfiguration(setOf(R.id.eventsFragment, R.id.profileFragment))
                setSupportActionBar(main_toolbarView)
                setupActionBarWithNavController(navController, appBarConfiguration)
            }
        }
    }
}