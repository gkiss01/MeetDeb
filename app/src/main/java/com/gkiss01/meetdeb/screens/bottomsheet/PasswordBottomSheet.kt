package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.gkiss01.meetdeb.databinding.BottomsheetProfilePasswordBinding
import com.gkiss01.meetdeb.screens.fragment.hideKeyboard
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.UpdateViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_profile_password.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswordBottomSheet: BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetProfilePasswordBinding
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: UpdateViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_profile_password, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!viewModelKoin.isUserInitialized())
            viewModelKoin.userLocal = UserRequest()

        binding.user = viewModelKoin.userLocal

        bspp_updateButton.attachTextChangeAnimator()
        bspp_updateButton.setOnClickListener {
            val isValidPasswordNew = validatePasswordNew()
            val isValidPasswordOld = validatePasswordOld()

            if (isValidPasswordNew && isValidPasswordOld) {
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
            bspp_updateButton.isEnabled = false
            bspp_updateButton.hideProgress(R.string.done)
            viewModelActivityKoin.setActiveUser(it)
            viewModelActivityKoin.setUserCredentials(null, viewModelKoin.userLocal.password)
            Handler().postDelayed({ findNavController().navigateUp() }, 500)
        }
    }

    private fun validatePasswordNew(): Boolean {
        val password = viewModelKoin.userLocal.password?.trim()

        return when {
            password.isNullOrEmpty() -> {
                bspp_newPassword.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                bspp_newPassword.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                bspp_newPassword.error = null
                true
            }
        }
    }

    private fun validatePasswordOld(): Boolean {
        val password = viewModelKoin.userLocal.name?.trim()

        return when {
            password.isNullOrEmpty() -> {
                bspp_oldPassword.error = getString(R.string.field_required)
                false
            }
            password.length < 8 -> {
                bspp_oldPassword.error = getString(R.string.min_password_length)
                false
            }
            else -> {
                bspp_oldPassword.error = null
                true
            }
        }
    }

    private fun showAnimation() {
        bspp_updateButton.showProgress {
            buttonTextRes = R.string.profile_email_update_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        if (bspp_updateButton.isProgressActive()) bspp_updateButton.hideProgress(R.string.profile_email_update)
    }
}