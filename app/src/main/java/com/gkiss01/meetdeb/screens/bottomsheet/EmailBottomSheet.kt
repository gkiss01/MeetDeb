package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.apirequest.UserRequest
import com.gkiss01.meetdeb.data.apirequest.UserRequestType
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.moshi
import com.gkiss01.meetdeb.utils.hideKeyboard
import kotlinx.android.synthetic.main.bottomsheet_profile_email.*
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EmailBottomSheet: SuperBottomSheetFragment() {
    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Suppress("unused_parameter")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        bspe_updateButton.hideProgress(R.string.profile_email_update)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.ACTIVE_USER_UPDATED) {
            bspe_updateButton.hideProgress(R.string.done)
            Handler().postDelayed({ this.dismiss() }, 500)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bspe_updateButton.setOnClickListener {
            val isValidEmail = validateEmail()
            val isValidPassword = validatePassword()

            if (isValidEmail && isValidPassword) {
                val email = bspe_newEmail.editText?.text.toString().trim()
                val password = bspe_oldPassword.editText?.text.toString().trim()

                hideKeyboard(requireContext(), view)
                showAnimation()

                val basic = Credentials.basic(activityViewModel.activeUser.value!!.email, password)

                val userRequest = UserRequest(email, "________", "________", UserRequestType.EmailUpdate.ordinal)
                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
                val user = json.toRequestBody("application/json".toMediaTypeOrNull())
                MainActivity.instance.updateUser(basic, user)
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