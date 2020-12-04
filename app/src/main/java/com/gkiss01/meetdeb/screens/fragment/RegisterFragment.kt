package com.gkiss01.meetdeb.screens.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.databinding.FragmentRegisterBinding
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.utils.runDelayed
import com.gkiss01.meetdeb.viewmodels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModelKoin: RegisterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!viewModelKoin.isUserInitialized())
            viewModelKoin.userLocal = UserRequest()

        binding.user = viewModelKoin.userLocal

        rf_alreadyRegistered.setOnClickListener { findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()) }

        rf_registerButton.attachTextChangeAnimator()
        rf_registerButton.setOnClickListener {
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
            rf_registerButton.isEnabled = false
            rf_registerButton.hideProgress(R.string.done)
            runDelayed { findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()) }
        }
    }

    private fun validateEmail(): Boolean {
        val email = viewModelKoin.userLocal.email?.trim()

        return when {
            email.isNullOrEmpty() -> {
                rf_email.error = getString(R.string.field_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                rf_email.error = getString(R.string.invalid_email)
                false
            }
            else -> {
                rf_email.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = viewModelKoin.userLocal.password?.trim()

        return when {
            password.isNullOrEmpty() -> {
                rf_password.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                rf_password.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                rf_password.error = null
                true
            }
        }
    }

    private fun validateName(): Boolean {
        val name = viewModelKoin.userLocal.name?.trim()

        return when {
            name.isNullOrEmpty() -> {
                rf_name.error = getString(R.string.field_required)
                false
            }
            name.length < 4 -> {
                rf_name.error = getString(R.string.min_name_length)
                false
            }
            name.length > 80 -> {
                rf_name.error = getString(R.string.max_name_length)
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

    private fun hideAnimation() {
        if (rf_registerButton.isProgressActive()) rf_registerButton.hideProgress(R.string.register_title)
    }
}

fun Fragment.hideKeyboard() {
    val inputMethodManager = this.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.view?.windowToken, 0)
}