package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.isProgressActive
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.DeleteViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_profile_delete.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteBottomSheet: BottomSheetDialogFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: DeleteViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_delete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        debsf_deleteButton.attachTextChangeAnimator()
        debsf_deleteButton.setOnClickListener {
            viewModelKoin.deleteUser()
        }
        debsf_cancelButton.setOnClickListener { findNavController().navigateUp() }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Felhasználó törlése
        viewModelKoin.currentlyDeleting.observe(viewLifecycleOwner) {
            if (it) showAnimation() else hideAnimation()
        }

        viewModelKoin.operationSuccessful.observeEvent(viewLifecycleOwner) {
            debsf_deleteButton.isEnabled = false
            debsf_deleteButton.hideProgress(R.string.done)
            Handler().postDelayed({
                viewModelActivityKoin.logout()
                mainActivity?.changeNavGraphToStart()
            }, 500)
        }
    }

    private fun showAnimation() {
        debsf_deleteButton.showProgress {
            buttonTextRes = R.string.profile_delete_yes
            progressColor = Color.BLACK
        }
    }

    private fun hideAnimation() {
        if (debsf_deleteButton.isProgressActive()) debsf_deleteButton.hideProgress(R.string.profile_delete_yes)
    }
}