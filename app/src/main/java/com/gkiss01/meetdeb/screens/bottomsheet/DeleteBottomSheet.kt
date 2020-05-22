package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.screens.fragment.SuccessObserver
import kotlinx.android.synthetic.main.bottomsheet_profile_delete.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DeleteBottomSheet: SuperBottomSheetFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_delete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        debsf_cancelButton.setOnClickListener { this.dismiss() }

        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.PENDING -> findNavController().navigate(R.id.registerFragment)
                else -> {}
            }
        })

        val deleteObserver = SuccessObserver {
            when (it.status) {
                Status.SUCCESS -> {
                    debsf_deleteButton.hideProgress(R.string.done)
                    Handler().postDelayed({ viewModelActivityKoin.resetLiveData() }, 500)
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    debsf_deleteButton.hideProgress(R.string.profile_delete_yes)
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_DeleteBottomSheet", "Deleting user...")
                    showAnimation()
                }
                else -> {}
            }
        }

        debsf_deleteButton.attachTextChangeAnimator()
        debsf_deleteButton.setOnClickListener {
            viewModelActivityKoin.deleteUser().observe(viewLifecycleOwner, deleteObserver)
        }
    }

    private fun showAnimation() {
        debsf_deleteButton.showProgress {
            buttonTextRes = R.string.profile_delete_yes
            progressColor = Color.BLACK
        }
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun isSheetCancelableOnTouchOutside() = false
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 335F,
        requireContext().resources.displayMetrics).toInt()
}