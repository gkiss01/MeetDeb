package com.gkiss01.meetdeb.screens.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.data.fastadapter.Participant
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.viewmodels.ParticipantsViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.android.synthetic.main.fragment_participants.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ParticipantsDialogFragment : DialogFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: ParticipantsViewModel by viewModel { parametersOf(viewModelActivityKoin.getBasic()) }

    private val itemAdapter = ItemAdapter<Participant>()
    private val headerAdapter = ItemAdapter<ProgressItem>()
    private lateinit var fastAdapter: GenericFastAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!viewModelKoin.isEventInitialized()) {
            viewModelKoin.event = requireArguments().getSerializable("event") as Event
            viewModelKoin.getParticipants()
        }

        fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter))
        fastAdapter.attachDefaultListeners = false
        pf_participantsRecyclerView.adapter = fastAdapter

        pf_participantsRecyclerView.layoutManager = LinearLayoutManager(context)

        pf_participantsRecyclerView.setItemViewCacheSize(20)
        pf_participantsRecyclerView.itemAnimator = null

        viewModelKoin.participants.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    FastAdapterDiffUtil[itemAdapter] = it.data!!
                    headerAdapter.clear()
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    headerAdapter.clear()
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_ParticipantsDialogFragment", "Participants are loading...")
                    headerAdapter.clear()
                    headerAdapter.add(ProgressItem())
                }
                else -> {}
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onResume() {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), FrameLayout.LayoutParams.WRAP_CONTENT)
        super.onResume()
    }
}
