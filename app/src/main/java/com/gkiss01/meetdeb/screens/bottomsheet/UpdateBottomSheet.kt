package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.BottomsheetProfileUpdateBinding
import com.gkiss01.meetdeb.screens.fragment.ProfileFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UpdateBottomSheet: BottomSheetDialogFragment() {
    private var _binding: BottomsheetProfileUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomsheetProfileUpdateBinding.bind(view)

        binding.emailButton.setOnClickListener {
            findNavController().navigateUp()
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToEmailBottomSheet())
        }
        binding.passwordButton.setOnClickListener {
            findNavController().navigateUp()
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToPasswordBottomSheet())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}