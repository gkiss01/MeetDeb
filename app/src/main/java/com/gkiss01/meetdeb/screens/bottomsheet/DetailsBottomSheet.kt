package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.BottomsheetEventDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetailsBottomSheet: BottomSheetDialogFragment() {
    private var _binding: BottomsheetEventDetailsBinding? = null
    private val binding get() = _binding!!

    private val safeArgs: DetailsBottomSheetArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomsheetEventDetailsBinding.bind(view)

        binding.event = safeArgs.event
        binding.participantsButton.setOnClickListener { findNavController().navigate(DetailsBottomSheetDirections.actionDetailsBottomSheetFragmentToParticipantsDialogFragment(safeArgs.event)) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}