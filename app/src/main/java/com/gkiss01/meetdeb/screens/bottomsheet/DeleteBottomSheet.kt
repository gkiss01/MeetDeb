package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.screens.fragment.SuccessObserver
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_profile_delete.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DeleteBottomSheet: BottomSheetDialogFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_delete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        debsf_cancelButton.setOnClickListener { this.dismiss() }

//        viewModelActivityKoin.activeUser.observe(viewLifecycleOwner, {
//            when (it.status) {
//                Status.PENDING -> findNavController().setGraph(R.navigation.navigation_graph_start)
//                else -> {}
//            }
//        })

        val deleteObserver = SuccessObserver {
            when (it.status) {
                Status.SUCCESS -> {
                    debsf_deleteButton.hideProgress(R.string.done)
                    Handler().postDelayed({ viewModelActivityKoin.resetActiveUser() }, 500)
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
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
}