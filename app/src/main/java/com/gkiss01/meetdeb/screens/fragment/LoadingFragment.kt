package com.gkiss01.meetdeb.screens.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.LoadingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoadingFragment : Fragment(R.layout.fragment_loading) {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: LoadingViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelKoin.checkUser()

        // Felhasználó ellenőrzése
        viewModelKoin.operationSuccessful.observeEvent(viewLifecycleOwner) {
            viewModelActivityKoin.setActiveUser(it)
            mainActivity?.changeNavGraphToMain()
        }

        viewModelKoin.operationUnsuccessful.observeEvent(viewLifecycleOwner) {
            findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToRegisterFragment())
        }
    }
}