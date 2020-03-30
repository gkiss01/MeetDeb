package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gkiss01.meetdeb.R
import kotlinx.android.synthetic.main.bottomsheet_profile_update.*

class UpdateBottomSheet: SuperBottomSheetFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bspu_emailButton.setOnClickListener {
            this.dismiss()
            findNavController().navigate(R.id.emailBottomSheet)
        }
        bspu_passwordButton.setOnClickListener {
            this.dismiss()
            findNavController().navigate(R.id.passwordBottomSheet)
        }
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.bottomsheet_corner_radius)
    override fun getPeekHeight() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 225F,
        context!!.resources.displayMetrics).toInt()
}