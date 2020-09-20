package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.screens.fragment.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_profile_password.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PasswordBottomSheet: BottomSheetDialogFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private lateinit var password: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val updateObserver = Observer<Resource<User>> {
            when (it.status) {
                Status.SUCCESS -> {
                    bspp_updateButton.hideProgress(R.string.done)
                    it.data?.let { user ->
                        viewModelActivityKoin.setActiveUser(user)
                        viewModelActivityKoin.setUserCredentials(null, password)
                    }
                    Handler().postDelayed({ this.dismiss() }, 500)
                }
                Status.ERROR -> {
                    val errorMessage = if (it.errorCode == ErrorCodes.USER_DISABLED_OR_NOT_VALID) getString(R.string.invalid_current_password) else it.errorMessage
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    bspp_updateButton.hideProgress(R.string.profile_email_update)
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_PasswordBottomSheet", "Updating user password...")
                    showAnimation()
                }
                else -> {}
            }
        }

        bspp_updateButton.attachTextChangeAnimator()
        bspp_updateButton.setOnClickListener {
            val isValidPasswordNew = validatePasswordNew()
            val isValidPasswordOld = validatePasswordOld()

            if (isValidPasswordNew && isValidPasswordOld) {
                password = bspp_newPassword.editText?.text.toString().trim()
                val currentPassword = bspp_oldPassword.editText?.text.toString().trim()

                hideKeyboard()

                viewModelActivityKoin.updateUser(currentPassword, null, password).observe(viewLifecycleOwner, updateObserver)
            }
        }
    }

    private fun validatePasswordNew(): Boolean {
        val password = bspp_newPassword.editText?.text.toString().trim()

        return when {
            password.isEmpty() -> {
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
        val password = bspp_oldPassword.editText?.text.toString().trim()

        return when {
            password.isEmpty() -> {
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
}