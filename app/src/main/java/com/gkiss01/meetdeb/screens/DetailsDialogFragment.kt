package com.gkiss01.meetdeb.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Event
import kotlinx.android.synthetic.main.details_fragment_bottomsheet.*
import org.threeten.bp.format.DateTimeFormatter

class DetailsDialogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val event = arguments!!.getSerializable("event") as Event

        val bottomSheet = DetailsBottomSheetFragment(event)
        bottomSheet.show(childFragmentManager, "DetailsBottomSheetFragment")
    }
}

class DetailsBottomSheetFragment(private val event: Event): SuperBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.details_fragment_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm")

        dfbs_userNameValue.text = event.username
        dfbs_venueValue.text = event.venue
        dfbs_dateValue.text = event.date.format(formatter)
        dfbs_descriptionValue.text = event.labels
        dfbs_participants.text = "Ott lesz ${event.participants} ember"

        dfbs_participantsCheck.setOnClickListener {
            val participantsDialogFragment = ParticipantsDialogFragment()
            participantsDialogFragment.show(childFragmentManager, "participantsDialogFragment")
            MainActivity.instance.showParticipants(event.id)
        }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)
}