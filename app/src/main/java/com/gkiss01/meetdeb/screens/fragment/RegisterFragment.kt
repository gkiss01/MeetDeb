package com.gkiss01.meetdeb.screens.fragment

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
import com.gkiss01.meetdeb.data.apirequest.UserRequestType
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.moshi
import com.gkiss01.meetdeb.utils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_register.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RegisterFragment : Fragment(R.layout.fragment_register) {
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
            rf_registerButton.hideProgress(R.string.done)
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
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()
            val isValidName = validateName()

            if (isValidEmail && isValidPassword && isValidName) {
                val email = rf_email.editText?.text.toString().trim()
                val password = rf_password.editText?.text.toString().trim()
                val name = rf_name.editText?.text.toString().trim()

                hideKeyboard(context!!, view)
                showAnimation()

                val userRequest = UserRequest(email, password, name, UserRequestType.Create.ordinal)
                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
                val user = json.toRequestBody("application/json".toMediaTypeOrNull())
                MainActivity.instance.createUser(user)
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = rf_email.editText?.text.toString().trim()

        return when {
            email.isEmpty() -> {
                rf_email.error = "A mezőt kötelező kitölteni!"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                rf_email.error = "Az email cím nem valódi!"
                false
            }
            else -> {
                rf_email.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = rf_password.editText?.text.toString().trim()

        return when {
            password.isEmpty() -> {
                rf_password.error = "A mezőt kötelező kitölteni!"
                false
            }
            password.length < 8 -> {
                rf_password.error = "A jelszó min. 8 karakter lehet!"
                false
            }
            else -> {
                rf_password.error = null
                true
            }
        }
    }

    private fun validateName(): Boolean {
        val name = rf_name.editText?.text.toString().trim()

        return when {
            name.isEmpty() -> {
                rf_name.error = "A mezőt kötelező kitölteni!"
                false
            }
            name.length < 4 -> {
                rf_name.error = "A név min. 4 karakter lehet!"
                false
            }
            name.length > 80 -> {
                rf_name.error = "A név max. 80 karakter lehet!"
                false
            }
            else -> {
                rf_name.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        rf_registerButton.showProgress {
            buttonTextRes = R.string.register_create_waiting
            progressColor = Color.WHITE
        }
    }
}