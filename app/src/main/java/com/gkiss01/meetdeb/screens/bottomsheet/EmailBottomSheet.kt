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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.User
import com.gkiss01.meetdeb.network.Resource
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.screens.fragment.hideKeyboard
import kotlinx.android.synthetic.main.bottomsheet_profile_email.*

class EmailBottomSheet: SuperBottomSheetFragment() {
    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val updateObserver = Observer<Resource<User>> {
            when (it.status) {
                Status.SUCCESS -> {
                    bspe_updateButton.hideProgress(R.string.done)
                    it.data?.let { user -> activityViewModel.setActiveUser(user) }
                    Handler().postDelayed({ this.dismiss() }, 500)
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
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
                val email = bspe_newEmail.editText?.text.toString().trim()
                val currentPassword = bspe_oldPassword.editText?.text.toString().trim()

                hideKeyboard()

                activityViewModel.updateUser(currentPassword, email, null).observe(viewLifecycleOwner, updateObserver)
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
                bspe_newEmail.error = "A mezőt kötelező kitölteni!"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                bspe_newEmail.error = "Az email cím nem valódi!"
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
                bspe_oldPassword.error = "A mezőt kötelező kitölteni!"
                false
            }
            password.length < 8 -> {
                bspe_oldPassword.error = "A jelszó min. 8 karakter lehet!"
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