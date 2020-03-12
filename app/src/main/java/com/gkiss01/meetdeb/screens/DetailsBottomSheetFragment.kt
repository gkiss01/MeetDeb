package com.gkiss01.meetdeb.screens

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.utils.formatDate
import kotlinx.android.synthetic.main.details_fragment_bottomsheet.*

class DetailsBottomSheetFragment: SuperBottomSheetFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.details_fragment_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val event = arguments!!.getSerializable("event") as Event

        dfbs_userNameValue.text = event.username
        dfbs_venueValue.text = event.venue
        dfbs_dateValue.text = formatDate(event.date)
        dfbs_descriptionValue.text = event.description
        dfbs_participants.text = "Ott lesz ${event.participants} ember"

        dfbs_participantsCheck.setOnClickListener {
            MainActivity.instance.showParticipants(event.id)

            findNavController().navigate(R.id.participantsDialogFragment)
        }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)

    override fun getPeekHeight() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 105F,
        context!!.resources.displayMetrics).toInt()
}