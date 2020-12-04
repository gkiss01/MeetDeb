package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.format
import com.gkiss01.meetdeb.databinding.BottomsheetEventDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetailsBottomSheet: BottomSheetDialogFragment() {
    private var binding: BottomsheetEventDetailsBinding? = null
    private val safeArgs: DetailsBottomSheetArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_event_details, container, false)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BottomsheetEventDetailsBinding.bind(view)
        this.binding = binding
        val event = safeArgs.event

        binding.usernameLabel.text = event.username
        binding.venueLabel.text = event.venue
        binding.dateLabel.text = event.date.format()
        binding.descriptionLabel.text = event.description
        binding.participantsLabel.text = getString(R.string.event_participants, event.participants)

        binding.participantsButton.setOnClickListener { findNavController().navigate(DetailsBottomSheetDirections.actionDetailsBottomSheetFragmentToParticipantsDialogFragment(event)) }
    }
}