package com.gkiss01.meetdeb.screens.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_register.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val viewModelKoin: ActivityViewModel by sharedViewModel()

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

                hideKeyboard(requireContext(), view)

                viewModelKoin.createUser(email, password, name)
            }
        }

        viewModelKoin.activeUser.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    rf_registerButton.hideProgress(R.string.done)
                    Handler().postDelayed({ findNavController().navigate(R.id.loginFragment) }, 500)
                }
                Status.ERROR -> {
                    viewModelKoin.resetLiveData()
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    rf_registerButton.hideProgress(R.string.register_title)
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_RegisterFragment", "Creating user...")
                    showAnimation()
                }
                else -> {}
            }
        })
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