package com.gkiss01.meetdeb.screens.bottomsheet

import android.graphics.Color
import android.os.Bundle
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
import com.gkiss01.meetdeb.databinding.BottomsheetProfileDeleteBinding
import com.gkiss01.meetdeb.utils.mainActivity
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.utils.runDelayed
import com.gkiss01.meetdeb.viewmodels.DeleteViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteBottomSheet: BottomSheetDialogFragment() {
    private var _binding: BottomsheetProfileDeleteBinding? = null
    private val binding get() = _binding!!
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: DeleteViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_delete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomsheetProfileDeleteBinding.bind(view)

        binding.deleteButton.attachTextChangeAnimator()
        binding.deleteButton.setOnClickListener {
            viewModelKoin.deleteUser()
        }
        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }

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
            binding.deleteButton.isEnabled = false
            binding.deleteButton.hideProgress(R.string.done)
            runDelayed {
                viewModelActivityKoin.logout()
                mainActivity?.changeNavGraphToStart()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAnimation() {
        binding.deleteButton.showProgress {
            buttonTextRes = R.string.profile_delete_yes
            progressColor = Color.BLACK
        }
    }

    private fun hideAnimation() {
        if (binding.deleteButton.isProgressActive()) binding.deleteButton.hideProgress(R.string.profile_delete_yes)
    }
}