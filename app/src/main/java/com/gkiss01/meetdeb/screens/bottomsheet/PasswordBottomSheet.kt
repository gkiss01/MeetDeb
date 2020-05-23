package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.screens.fragment.hideKeyboard
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.bottomsheet_profile_password.*
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

class PasswordBottomSheet: SuperBottomSheetFragment() {
    private val moshi: Moshi by inject()
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
        bspp_updateButton.hideProgress(R.string.profile_email_update)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.ACTIVE_USER_UPDATED) {
            bspp_updateButton.hideProgress(R.string.done)
            Handler().postDelayed({ this.dismiss() }, 500)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bspp_updateButton.setOnClickListener {
            val isValidPasswordNew = validatePasswordNew()
            val isValidPasswordOld = validatePasswordOld()

            if (isValidPasswordNew && isValidPasswordOld) {
                val newPassword = bspp_newPassword.editText?.text.toString().trim()
                val oldPassword = bspp_oldPassword.editText?.text.toString().trim()

                hideKeyboard()
                showAnimation()

                val basic = Credentials.basic(activityViewModel.activeUser.value!!.data!!.email, oldPassword)

//                val userRequest = UserRequest("unnecessary@email.com", newPassword, "________", UserRequestType.PasswordUpdate.ordinal)
//                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
//                val user = json.toRequestBody("application/json".toMediaTypeOrNull())
//                //MainActivity.instance.saveTempPassword(newPassword)
//                MainActivity.instance.updateUser(basic, user)
            }
        }
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 255F,
        requireContext().resources.displayMetrics).toInt()

    private fun validatePasswordNew(): Boolean {
        val password = bspp_newPassword.editText?.text.toString().trim()

        return when {
            password.isEmpty() -> {
                bspp_newPassword.error = "A mezőt kötelező kitölteni!"
                false
            }
            password.length < 8 -> {
                bspp_newPassword.error = "A jelszó min. 8 karakter lehet!"
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
                bspp_oldPassword.error = "A mezőt kötelező kitölteni!"
                false
            }
            password.length < 8 -> {
                bspp_oldPassword.error = "A jelszó min. 8 karakter lehet!"
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