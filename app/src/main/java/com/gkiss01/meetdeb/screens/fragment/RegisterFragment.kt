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
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.FragmentRegisterBinding
import com.gkiss01.meetdeb.utils.hideKeyboard
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.utils.runDelayed
import com.gkiss01.meetdeb.viewmodels.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModelKoin: RegisterViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        binding.user = viewModelKoin.userLocal

        binding.alreadyRegisteredLabel.setOnClickListener { findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()) }

        binding.registerButton.attachTextChangeAnimator()
        binding.registerButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()
            val isValidName = validateName()

            if (isValidEmail && isValidPassword && isValidName) {
                hideKeyboard()

                viewModelKoin.createUser()
            }
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Felhasználó létrehozása
        viewModelKoin.currentlyRegistering.observe(viewLifecycleOwner) {
            if (it) showAnimation() else hideAnimation()
        }

        viewModelKoin.operationSuccessful.observeEvent(viewLifecycleOwner) {
            binding.registerButton.isEnabled = false
            binding.registerButton.hideProgress(R.string.done)
            runDelayed { findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()) }
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

    private fun validateName(): Boolean {
        val name = viewModelKoin.userLocal.name?.trim()

        return when {
            name.isNullOrEmpty() -> {
                binding.nameField.error = getString(R.string.field_required)
                false
            }
            name.length < 4 -> {
                binding.nameField.error = getString(R.string.min_name_length)
                false
            }
            name.length > 80 -> {
                binding.nameField.error = getString(R.string.max_name_length)
                false
            }
            else -> {
                binding.nameField.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        binding.registerButton.showProgress {
            buttonTextRes = R.string.register_create_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        if (binding.registerButton.isProgressActive()) binding.registerButton.hideProgress(R.string.register_title)
    }
}