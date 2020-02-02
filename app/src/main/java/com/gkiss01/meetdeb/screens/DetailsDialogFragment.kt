package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.Event
import com.gkiss01.meetdeb.databinding.DetailsFragmentBinding

class DetailsDialogFragment : DialogFragment() {

    private lateinit var binding: DetailsFragmentBinding

    private lateinit var event: Event

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        event = arguments!!.getSerializable(EXTRA_EVENT) as Event

        binding = DataBindingUtil.inflate(inflater, R.layout.details_fragment, container, false)

        binding.event = event
        binding.participantsCheck.setOnClickListener {
            val participantsDialogFragment = ParticipantsDialogFragment()
            participantsDialogFragment.show(childFragmentManager, "participantsDialogFragment")
            MainActivity.instance.showParticipants(event.id)
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onResume() {
        dialog!!.window!!.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        super.onResume()
    }

    companion object {
        private const val EXTRA_EVENT = "evt"

        fun newInstance(event: Event): DetailsDialogFragment {
            val dialogFragment = DetailsDialogFragment()
            val args = Bundle().apply {
                putSerializable(EXTRA_EVENT, event)
            }
            dialogFragment.arguments = args
            return dialogFragment
        }
    }
}
