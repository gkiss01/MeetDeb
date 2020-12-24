package com.gkiss01.meetdeb.screens.flow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.FragmentFlowMainBinding
import com.gkiss01.meetdeb.utils.mainActivity

class MainFlowFragment : Fragment(R.layout.fragment_flow_main) {
    private var _binding: FragmentFlowMainBinding? = null
    private val binding get() = _binding!!

    private val navController: NavController? by lazy { childFragmentManager.findFragmentById(R.id.main_nav_host_fragment)?.findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFlowMainBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        navController?.let {
            binding.mainNavigationView.setupWithNavController(it)
            binding.mainNavigationView.setOnNavigationItemReselectedListener {  }

            mainActivity?.apply {
                val appBarConfiguration = AppBarConfiguration(setOf(R.id.eventsFragment, R.id.profileFragment))
                setSupportActionBar(binding.mainToolbarView)
                setupActionBarWithNavController(it, appBarConfiguration)
            }
        }
    }
}