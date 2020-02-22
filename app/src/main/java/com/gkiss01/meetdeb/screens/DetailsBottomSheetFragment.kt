package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.network.dateFormatter
import kotlinx.android.synthetic.main.details_fragment_bottomsheet.*

class DetailsBottomSheetFragment(private val event: Event): SuperBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.details_fragment_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dfbs_userNameValue.text = event.username
        dfbs_venueValue.text = event.venue
        dfbs_dateValue.text = event.date.format(dateFormatter)
        dfbs_descriptionValue.text = event.labels
        dfbs_participants.text = "Ott lesz ${event.participants} ember"

        dfbs_participantsCheck.setOnClickListener {
            val participantsDialogFragment = ParticipantsDialogFragment()
            participantsDialogFragment.show(childFragmentManager, "participantsDialogFragment")
            MainActivity.instance.showParticipants(event.id)
        }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)

    override fun getPeekHeight() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 105F,
        context!!.resources.displayMetrics).toInt()
}