package com.gkiss01.meetdeb.screens

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LoadingFragment : Fragment(R.layout.loading_fragment) {

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.USER_DISABLED_OR_NOT_VALID) {
            val action = LoadingFragmentDirections.actionLoadingFragmentToRegisterFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.NAVIGATE_TO_EVENTS_FRAGMENT) {
            val action = LoadingFragmentDirections.actionLoadingFragmentToEventsFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }
}
