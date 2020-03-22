package com.gkiss01.meetdeb.screens.dialog

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
import com.gkiss01.meetdeb.data.ParticipantList
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.viewmodels.ParticipantsDialogViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.android.synthetic.main.fragment_participants.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ParticipantsDialogFragment : DialogFragment() {
    private val viewModel: ParticipantsDialogViewModel by viewModels()

    private val itemAdapter = ItemAdapter<Participant>()
    private val headerAdapter = ItemAdapter<ProgressItem>()
    private lateinit var fastAdapter: GenericFastAdapter

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
        viewModel.setParticipants(participants.participants)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.NO_PARTICIPANTS_FOUND) {
            this.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter))
        fastAdapter.attachDefaultListeners = false
        pf_participantsRecyclerView.adapter = fastAdapter

        pf_participantsRecyclerView.layoutManager = LinearLayoutManager(context)

        pf_participantsRecyclerView.setItemViewCacheSize(20)
        pf_participantsRecyclerView.itemAnimator = null

        if (viewModel.participants.value == null || viewModel.participants.value!!.isEmpty()) {
            headerAdapter.clear()
            headerAdapter.add(ProgressItem())
        }

        viewModel.participants.observe(viewLifecycleOwner, Observer {
            FastAdapterDiffUtil[itemAdapter] = it
            headerAdapter.clear()
        })
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
