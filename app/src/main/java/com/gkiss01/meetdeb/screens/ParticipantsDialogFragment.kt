package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.ParticipantEntryAdapter
import com.gkiss01.meetdeb.data.ParticipantList
import com.gkiss01.meetdeb.network.ErrorCodes
import kotlinx.android.synthetic.main.participants_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ParticipantsDialogFragment : DialogFragment() {
    private val viewModel: ParticipantsDialogViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onParticipantsReceived(participants: ParticipantList) {
        viewModel.participants.value = participants.participants
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.NO_PARTICIPANTS_FOUND) {
            this.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.participants_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewAdapter = ParticipantEntryAdapter()

        if (viewModel.participants.value == null || viewModel.participants.value!!.isEmpty())
            viewAdapter.addLoading()

        viewModel.participants.observe(viewLifecycleOwner, Observer {
            viewAdapter.addParticipants(it)
        })

        pf_participantsRecyclerView.adapter = viewAdapter
        pf_participantsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onResume() {
        dialog!!.window!!.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), FrameLayout.LayoutParams.WRAP_CONTENT)
        super.onResume()
    }
}
