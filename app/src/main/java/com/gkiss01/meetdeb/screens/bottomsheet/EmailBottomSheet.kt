package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.apirequest.UserRequest
import com.gkiss01.meetdeb.data.apirequest.UserRequestType
import com.gkiss01.meetdeb.network.NavigationCode
import com.gkiss01.meetdeb.network.moshi
import com.gkiss01.meetdeb.utils.*
import kotlinx.android.synthetic.main.bottomsheet_profile_email.*
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EmailBottomSheet: SuperBottomSheetFragment() {
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.ACTIVE_USER_UPDATED) {
            setSavedUser(context!!, getActiveUser()!!.email, getSavedPassword(context!!))
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
            var error = false
            val email = bspe_newEmail.text.toString()
            val password = bspe_oldPassword.text.toString()

            if (email.isEmpty()) {
                bspe_newEmail.error = "A mezőt kötelező kitölteni!"
                error = true
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                bspe_newEmail.error = "Az email cím nem valódi!"
                error = true
            }

            if (password.isEmpty()) {
                bspe_oldPassword.error = "A mezőt kötelező kitölteni!"
                error = true
            }

            if (!error) {
                hideKeyboard(context!!, view)
                showAnimation()

                val userRequest = UserRequest(email, "________", "________", UserRequestType.EmailUpdate.ordinal)
                val json = moshi.adapter(UserRequest::class.java).toJson(userRequest)
                val user = json.toRequestBody("application/json".toMediaTypeOrNull())

                val basic = Credentials.basic(getSavedUsername(context!!), password)

                MainActivity.instance.updateUser(basic, user)
            }
        }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 252F,
        context!!.resources.displayMetrics).toInt()

    private fun showAnimation() {
        bspe_updateButton.showProgress {
            buttonTextRes = R.string.profile_email_update_waiting
            progressColor = Color.WHITE
        }
    }
}