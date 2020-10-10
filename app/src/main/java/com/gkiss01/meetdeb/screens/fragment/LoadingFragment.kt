package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.mainActivity
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoadingFragment : Fragment(R.layout.fragment_loading) {
    private val viewModelKoin: ActivityViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelKoin.getCurrentUser()

        viewModelKoin.activeUser.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> mainActivity?.changeNavGraphToMain()
                Status.ERROR -> {
                    if (it.errorCode != ErrorCodes.USER_DISABLED_OR_NOT_VALID)
                        Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    viewModelKoin.resetActiveUser()
                    findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToRegisterFragment())
                }
                Status.LOADING -> Log.d("MeetDebLog_LoadingFragment", "User is loading...")
                else -> {}
            }
        })
    }
}