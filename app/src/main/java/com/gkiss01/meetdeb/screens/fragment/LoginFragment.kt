package com.gkiss01.meetdeb.screens.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.databinding.FragmentLoginBinding
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: LoginViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!viewModelKoin.isUserInitialized())
            viewModelKoin.userLocal = UserRequest()

        binding.user = viewModelKoin.userLocal

        lf_notRegistered.setOnClickListener { findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment()) }

        lf_loginButton.attachTextChangeAnimator()
        lf_loginButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()

            if (isValidEmail && isValidPassword) {
                hideKeyboard()

                viewModelKoin.loginUser()
            }
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Felhasználó ellenőrzése
        viewModelKoin.currentlyLoggingIn.observe(viewLifecycleOwner) {
            if (it) showAnimation() else hideAnimation()
        }

        viewModelKoin.operationSuccessful.observeEvent(viewLifecycleOwner) {
            viewModelActivityKoin.setActiveUser(it)
            mainActivity?.changeNavGraphToMain()
        }
    }

    private fun validateEmail(): Boolean {
        val email = viewModelKoin.userLocal.email?.trim()

        return when {
            email.isNullOrEmpty() -> {
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
        val password = viewModelKoin.userLocal.password?.trim()

        return when {
            password.isNullOrEmpty() -> {
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

    private fun hideAnimation() {
        if (lf_loginButton.isProgressActive()) lf_loginButton.hideProgress(R.string.login_title)
    }
}