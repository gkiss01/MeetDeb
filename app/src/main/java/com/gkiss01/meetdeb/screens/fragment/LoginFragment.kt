package com.gkiss01.meetdeb.screens.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.utils.hideKeyboard
import com.gkiss01.meetdeb.utils.setSavedUser
import kotlinx.android.synthetic.main.fragment_login.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LoginFragment : Fragment(R.layout.fragment_login) {
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
            lf_loginButton.hideProgress(R.string.login_title)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.ACTIVE_USER_UPDATED)
            findNavController().navigate(R.id.eventsFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lf_notRegistered.setOnClickListener { findNavController().popBackStack() }

        lf_loginButton.attachTextChangeAnimator()
        lf_loginButton.setOnClickListener {
            var error = false
            val email = lf_email.text.toString()
            val password = lf_password.text.toString()

            if (email.isEmpty()) {
                lf_email.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                lf_email.error = "Az email cím nem valódi!"
                error = true
            }

            if (password.isEmpty()) {
                lf_password.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (password.length < 8) {
                lf_password.error = "A jelszó min. 8 karakter lehet!"
                error = true
            }

            if (!error) {
                hideKeyboard(context!!, view)
                showAnimation()

                setSavedUser(context!!, email, password)
                MainActivity.instance.checkUser()
            }
        }
    }

    private fun showAnimation() {
        lf_loginButton.showProgress {
            buttonTextRes = R.string.login_waiting
            progressColor = Color.WHITE
        }
    }
}