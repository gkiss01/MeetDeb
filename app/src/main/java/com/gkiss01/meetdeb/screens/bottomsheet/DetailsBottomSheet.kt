package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.utils.formatDate
import kotlinx.android.synthetic.main.bottomsheet_event_details.*

class DetailsBottomSheet: SuperBottomSheetFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val event = requireArguments().getSerializable("event") as Event

        dfbs_userNameValue.text = event.username
        dfbs_venueValue.text = event.venue
        dfbs_dateValue.text = formatDate(event.date)
        dfbs_descriptionValue.text = event.description
        dfbs_participants.text = "Ott lesz ${event.participants} ember"

        dfbs_participantsCheck.setOnClickListener { findNavController().navigate(DetailsBottomSheetDirections.actionDetailsBottomSheetFragmentToParticipantsDialogFragment(event)) }
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 105F,
        requireContext().resources.displayMetrics).toInt()
}