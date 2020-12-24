package com.gkiss01.meetdeb.screens.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.request.UserRequest
import com.gkiss01.meetdeb.databinding.FragmentLoginBinding
import com.gkiss01.meetdeb.utils.hideKeyboard
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        if (!viewModelKoin.isUserInitialized())
            viewModelKoin.userLocal = UserRequest()

        binding.user = viewModelKoin.userLocal

        binding.notRegisteredLabel.setOnClickListener { findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment()) }

        binding.loginButton.attachTextChangeAnimator()
        binding.loginButton.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateEmail(): Boolean {
        val email = viewModelKoin.userLocal.email?.trim()

        return when {
            email.isNullOrEmpty() -> {
                binding.emailField.error = getString(R.string.field_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailField.error = getString(R.string.invalid_email)
                false
            }
            else -> {
                binding.emailField.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = viewModelKoin.userLocal.password?.trim()

        return when {
            password.isNullOrEmpty() -> {
                binding.passwordField.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                binding.passwordField.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                binding.passwordField.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        binding.loginButton.showProgress {
            buttonTextRes = R.string.login_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        if (binding.loginButton.isProgressActive()) binding.loginButton.hideProgress(R.string.login_title)
    }
}