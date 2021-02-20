package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.BottomsheetProfileEmailBinding
import com.gkiss01.meetdeb.utils.hideKeyboard
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.utils.runDelayed
import com.gkiss01.meetdeb.viewmodels.UpdateViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailBottomSheet: BottomSheetDialogFragment() {
    private var _binding: BottomsheetProfileEmailBinding? = null
    private val binding get() = _binding!!

    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: UpdateViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomsheetProfileEmailBinding.bind(view)

        binding.user = viewModelKoin.userLocal

        binding.updateButton.attachTextChangeAnimator()
        binding.updateButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()

            if (isValidEmail && isValidPassword) {
                hideKeyboard()

                viewModelKoin.updateUser()
            }
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Felhasználó frissítése
        viewModelKoin.currentlyUpdating.observe(viewLifecycleOwner) {
            if (it) showAnimation() else hideAnimation()
        }

        viewModelKoin.operationSuccessful.observeEvent(viewLifecycleOwner) {
            binding.updateButton.isEnabled = false
            binding.updateButton.hideProgress(R.string.done)
            viewModelActivityKoin.setActiveUser(it)
            viewModelActivityKoin.setUserCredentials(viewModelKoin.userLocal.email, null)
            runDelayed { findNavController().navigateUp() }
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
                binding.newEmailField.error = getString(R.string.field_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.newEmailField.error = getString(R.string.invalid_email)
                false
            }
            else -> {
                binding.newEmailField.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = viewModelKoin.userLocal.name?.trim()

        return when {
            password.isNullOrEmpty() -> {
                binding.oldPasswordField.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                binding.oldPasswordField.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                binding.oldPasswordField.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        binding.updateButton.showProgress {
            buttonTextRes = R.string.profile_email_update_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        if (binding.updateButton.isProgressActive()) binding.updateButton.hideProgress(R.string.profile_email_update)
    }
}