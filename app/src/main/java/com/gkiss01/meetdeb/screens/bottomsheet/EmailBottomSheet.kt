package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gkiss01.meetdeb.R
import kotlinx.android.synthetic.main.bottomsheet_profile_email.*

class EmailBottomSheet: SuperBottomSheetFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        bspe_updateButton.setOnClickListener {  }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 252F,
        context!!.resources.displayMetrics).toInt()
}