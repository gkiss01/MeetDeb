package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
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
import kotlinx.android.synthetic.main.bottomsheet_profile_email.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EmailBottomSheet: SuperBottomSheetFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private lateinit var email: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val updateObserver = Observer<Resource<User>> {
            when (it.status) {
                Status.SUCCESS -> {
                    bspe_updateButton.hideProgress(R.string.done)
                    it.data?.let { user ->
                        viewModelActivityKoin.setActiveUser(user)
                        viewModelActivityKoin.setUserCredentials(email, null)
                    }
                    Handler().postDelayed({ this.dismiss() }, 500)
                }
                Status.ERROR -> {
                    val errorMessage = if (it.errorCode == ErrorCodes.USER_DISABLED_OR_NOT_VALID) getString(R.string.invalid_current_password) else it.errorMessage
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    bspe_updateButton.hideProgress(R.string.profile_email_update)
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_EmailBottomSheet", "Updating user email...")
                    showAnimation()
                }
                else -> {}
            }
        }

        bspe_updateButton.attachTextChangeAnimator()
        bspe_updateButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()

            if (isValidEmail && isValidPassword) {
                email = bspe_newEmail.editText?.text.toString().trim()
                val currentPassword = bspe_oldPassword.editText?.text.toString().trim()

                hideKeyboard()

                viewModelActivityKoin.updateUser(currentPassword, email, null).observe(viewLifecycleOwner, updateObserver)
            }
        }
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 255F,
        requireContext().resources.displayMetrics).toInt()

    private fun validateEmail(): Boolean {
        val email = bspe_newEmail.editText?.text.toString().trim()

        return when {
            email.isEmpty() -> {
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
        val password = bspe_oldPassword.editText?.text.toString().trim()

        return when {
            password.isEmpty() -> {
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
}