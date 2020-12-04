package com.gkiss01.meetdeb.screens.flow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.FragmentFlowMainBinding
import com.gkiss01.meetdeb.utils.mainActivity

class MainFlowFragment : Fragment(R.layout.fragment_flow_main) {
    private var binding: FragmentFlowMainBinding? = null

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentFlowMainBinding.bind(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val binding = this.binding ?: return
        childFragmentManager.findFragmentById(R.id.main_nav_host_fragment)?.let {
            val navController = it.findNavController()
            binding.mainNavigationView.setupWithNavController(navController)
            binding.mainNavigationView.setOnNavigationItemReselectedListener {  }

            mainActivity?.apply {
                val appBarConfiguration = AppBarConfiguration(setOf(R.id.eventsFragment, R.id.profileFragment))
                setSupportActionBar(binding.mainToolbarView)
                setupActionBarWithNavController(navController, appBarConfiguration)
            }
        }
    }
}