package com.gkiss01.meetdeb.screens.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.mainActivity
import kotlinx.android.synthetic.main.fragment_login.*
import okhttp3.Credentials
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModelKoin: ActivityViewModel by sharedViewModel()
    private lateinit var email: String
    private lateinit var password: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lf_notRegistered.setOnClickListener { findNavController().popBackStack() }

        lf_loginButton.attachTextChangeAnimator()
        lf_loginButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()

            if (isValidEmail && isValidPassword) {
                email = lf_email.editText?.text.toString().trim()
                password = lf_password.editText?.text.toString().trim()
                val basic = Credentials.basic(email, password)

                hideKeyboard()

                viewModelKoin.getCurrentUser(basic)
            }
        }

        viewModelKoin.activeUser.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    viewModelKoin.setUserCredentials(email, password)
                    mainActivity?.changeNavGraphToMain()
                }
                Status.ERROR -> {
                    val errorMessage = if (it.errorCode == ErrorCodes.USER_DISABLED_OR_NOT_VALID) getString(R.string.invalid_credentials) else it.errorMessage
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    lf_loginButton.hideProgress(R.string.login_title)
                    viewModelKoin.resetLiveData()
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_LoginFragment", "User is loading...")
                    showAnimation()
                }
                else -> {}
            }
        })
    }

    private fun validateEmail(): Boolean {
        val email = lf_email.editText?.text.toString().trim()

        return when {
            email.isEmpty() -> {
                lf_email.error = getString(R.string.field_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                lf_email.error = getString(R.string.invalid_email)
                false
            }
            else -> {
                lf_email.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = lf_password.editText?.text.toString().trim()

        return when {
            password.isEmpty() -> {
                lf_password.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                lf_password.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                lf_password.error = null
                true
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