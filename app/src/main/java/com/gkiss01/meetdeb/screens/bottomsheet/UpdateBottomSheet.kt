package com.gkiss01.meetdeb.screens.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gkiss01.meetdeb.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_profile_update.*

class UpdateBottomSheet: BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottomsheet_profile_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bspu_emailButton.setOnClickListener {
            this.dismiss()
            findNavController().navigate(UpdateBottomSheetDirections.actionUpdateBottomSheetFragmentToEmailBottomSheet())
        }
        bspu_passwordButton.setOnClickListener {
            this.dismiss()
            findNavController().navigate(UpdateBottomSheetDirections.actionUpdateBottomSheetFragmentToPasswordBottomSheet())
        }
    }
}