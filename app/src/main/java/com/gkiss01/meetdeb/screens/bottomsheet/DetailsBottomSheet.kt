package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.format
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_event_details.*

class DetailsBottomSheet: BottomSheetDialogFragment() {
    private val safeArgs: DetailsBottomSheetArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val event = safeArgs.event

        dfbs_userNameValue.text = event.username
        dfbs_venueValue.text = event.venue
        dfbs_dateValue.text = event.date.format()
        dfbs_descriptionValue.text = event.description
        dfbs_participants.text = getString(R.string.event_participants, event.participants)

        dfbs_participantsCheck.setOnClickListener { findNavController().navigate(DetailsBottomSheetDirections.actionDetailsBottomSheetFragmentToParticipantsDialogFragment(event)) }
    }
}