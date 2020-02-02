package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.ParticipantEntryAdapter
import com.gkiss01.meetdeb.data.ParticipantList
import com.gkiss01.meetdeb.databinding.ParticipantsFragmentBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ParticipantsDialogFragment : DialogFragment() {

    private lateinit var binding: ParticipantsFragmentBinding
    private lateinit var viewModel: ParticipantsDialogViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.participants_fragment, container, false)
        viewModel = ViewModelProvider(this).get(ParticipantsDialogViewModel::class.java)

        val viewAdapter = ParticipantEntryAdapter()

        if (viewModel.participants.value == null || viewModel.participants.value!!.isEmpty())
            viewAdapter.addLoading()

        viewModel.participants.observe(this, Observer {
            viewAdapter.addParticipants(it)
        })

        binding.participantsRecyclerView.adapter = viewAdapter
        binding.participantsRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
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
