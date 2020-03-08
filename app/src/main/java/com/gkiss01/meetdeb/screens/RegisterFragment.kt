package com.gkiss01.meetdeb.screens

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.apirequest.UserRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.moshi
import com.gkiss01.meetdeb.utils.hideKeyboard
import kotlinx.android.synthetic.main.register_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RegisterFragment : Fragment(R.layout.register_fragment) {
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
        if (errorCode == ErrorCodes.EMAIL_ALREADY_IN_USE || errorCode == ErrorCodes.BAD_REQUEST_FORMAT) {
            rf_registerButton.hideProgress(R.string.register_title)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.NAVIGATE_TO_LOGIN_FRAGMENT) {
            rf_registerButton.hideProgress(R.string.register_created)
            Handler().postDelayed({ findNavController().navigate(R.id.loginFragment) }, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finishAffinity()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rf_alreadyRegistered.setOnClickListener { findNavController().navigate(R.id.loginFragment) }

        rf_registerButton.attachTextChangeAnimator()
        rf_registerButton.setOnClickListener {
            var error = false
            val email = rf_email.text.toString()
            val password = rf_password.text.toString()
            val name = rf_name.text.toString()

            if (email.isEmpty()) {
                rf_email.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                rf_email.error = "Az email cím nem valódi!"
                error = true
            }

            if (password.isEmpty()) {
                rf_password.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (password.length < 8) {
                rf_password.error = "A jelszó min. 8 karakter lehet!"
                error = true
            }

            when {
                name.isEmpty() -> {
                    rf_name.error = "A mezőt kötelező kitölteni!"
                    error = true
                }
                name.length < 4 -> {
                    rf_name.error = "A név min. 4 karakter lehet!"
                    error = true
                }
                name.length > 80 -> {
                    rf_name.error = "A név max. 80 karakter lehet!"
                    error = true
                }
            }

            if (!error) {
                rf_registerButton.showProgress {
                    buttonTextRes = R.string.register_create_waiting
                    progressColor = Color.WHITE
                }

                hideKeyboard(context!!, view)

                val userRequest = UserRequest(email, password, name)
                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
                val user = json.toRequestBody("application/json".toMediaTypeOrNull())

                MainActivity.instance.uploadUser(user)
            }
        }
    }
}