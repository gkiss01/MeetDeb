package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.adapterrequest.DeleteUserRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.utils.setSavedUser
import kotlinx.android.synthetic.main.bottomsheet_profile_delete.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeleteBottomSheet: SuperBottomSheetFragment() {
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
    fun onDeleteRequestReceived(request: DeleteUserRequest) {
        debsf_deleteButton.hideProgress(R.string.done)
        Handler().postDelayed({
            setSavedUser(context!!, "", "")
            activityViewModel.activeUser.value = null
            activityViewModel.tempPassword = null
            activityViewModel.password = ""
            activityViewModel.basic = ""
            findNavController().navigate(R.id.registerFragment)
        }, 500)
    }

    @Suppress("unused_parameter")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        debsf_deleteButton.hideProgress(R.string.profile_delete_yes)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_delete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        debsf_cancelButton.setOnClickListener { this.dismiss() }

        debsf_deleteButton.attachTextChangeAnimator()
        debsf_deleteButton.setOnClickListener {
            MainActivity.instance.deleteUser(activityViewModel.activeUser.value!!.id)

            debsf_deleteButton.showProgress {
                buttonTextRes = R.string.profile_delete_yes
                progressColor = ContextCompat.getColor(context!!, R.color.black)
            }
        }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun isSheetCancelableOnTouchOutside() = false
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 335F,
        context!!.resources.displayMetrics).toInt()
}