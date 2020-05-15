package com.gkiss01.meetdeb.screens.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
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
import kotlinx.android.synthetic.main.fragment_login.*
import okhttp3.Credentials
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModelKoin: ActivityViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lf_notRegistered.setOnClickListener { findNavController().popBackStack() }

        lf_loginButton.attachTextChangeAnimator()
        lf_loginButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()

            if (isValidEmail && isValidPassword) {
                val email = lf_email.editText?.text.toString().trim()
                val password = lf_password.editText?.text.toString().trim()
                val basic = Credentials.basic(email, password)

                hideKeyboard(requireContext(), view)

                viewModelKoin.getCurrentUser(basic)
            }
        }

        viewModelKoin.activeUser.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> findNavController().navigate(R.id.eventsFragment)
                Status.ERROR -> {
                    viewModelKoin.resetLiveData()
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    lf_loginButton.hideProgress(R.string.login_title)
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
                lf_email.error = "A mezőt kötelező kitölteni!"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                lf_email.error = "Az email cím nem valódi!"
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
                lf_password.error = "A mezőt kötelező kitölteni!"
                false
            }
            password.length < 8 -> {
                lf_password.error = "A jelszó min. 8 karakter lehet!"
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