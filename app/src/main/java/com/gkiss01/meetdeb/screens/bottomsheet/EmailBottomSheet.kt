package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.request.UserRequest
import com.gkiss01.meetdeb.databinding.BottomsheetProfileEmailBinding
import com.gkiss01.meetdeb.screens.fragment.hideKeyboard
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.UpdateViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_profile_email.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailBottomSheet: BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetProfileEmailBinding
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: UpdateViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_profile_email, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!viewModelKoin.isUserInitialized())
            viewModelKoin.userLocal = UserRequest()

        binding.user = viewModelKoin.userLocal

        bspe_updateButton.attachTextChangeAnimator()
        bspe_updateButton.setOnClickListener {
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
            bspe_updateButton.isEnabled = false
            bspe_updateButton.hideProgress(R.string.done)
            viewModelActivityKoin.setActiveUser(it)
            viewModelActivityKoin.setUserCredentials(viewModelKoin.userLocal.email, null)
            Handler().postDelayed({ findNavController().navigateUp() }, 500)
        }
    }

    private fun validateEmail(): Boolean {
        val email = viewModelKoin.userLocal.email?.trim()

        return when {
            email.isNullOrEmpty() -> {
                bspe_newEmail.error = getString(R.string.field_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                bspe_newEmail.error = getString(R.string.invalid_email)
                false
            }
            else -> {
                bspe_newEmail.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = viewModelKoin.userLocal.name?.trim()

        return when {
            password.isNullOrEmpty() -> {
                bspe_oldPassword.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                bspe_oldPassword.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                bspe_oldPassword.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        bspe_updateButton.showProgress {
            buttonTextRes = R.string.profile_email_update_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        if (bspe_updateButton.isProgressActive()) bspe_updateButton.hideProgress(R.string.profile_email_update)
    }
}