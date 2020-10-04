package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.Status
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoadingFragment : Fragment(R.layout.fragment_loading) {
    private val viewModelKoin: ActivityViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelKoin.getCurrentUser()

        viewModelKoin.activeUser.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    findNavController().setGraph(R.navigation.navigation_graph_main)
                }
                Status.ERROR -> {
                    if (it.errorCode != ErrorCodes.USER_DISABLED_OR_NOT_VALID)
                        Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    viewModelKoin.resetLiveData()
                    findNavController().navigate(R.id.registerFragment)
                }
                Status.LOADING -> Log.d("MeetDebLog_LoadingFragment", "User is loading...")
                else -> {}
            }
        })
    }
}