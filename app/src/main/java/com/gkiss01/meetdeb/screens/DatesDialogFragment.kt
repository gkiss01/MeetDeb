package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.AdapterClickListener
import com.gkiss01.meetdeb.adapter.AdditionViewHolder
import com.gkiss01.meetdeb.adapter.DateEntryAdapter
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.gkiss01.meetdeb.data.DateList
import com.gkiss01.meetdeb.data.UpdateEventRequest
import com.gkiss01.meetdeb.databinding.DatesFragmentBinding
import com.gkiss01.meetdeb.network.ErrorCode
import com.gkiss01.meetdeb.network.NavigationCode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DatesDialogFragment : DialogFragment() {

    private lateinit var binding: DatesFragmentBinding
    private lateinit var viewModel: DatesDialogViewModel

    private var eventId: Long = -1L
    private var adapterPosition: Int = -1

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDatesReceived(dates: DateList) {
        viewModel.dates.value = dates.dates
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCode) {
        if (errorCode == ErrorCode.ERROR_DATE_CREATED) {
            val view = binding.datesRecyclerView.findViewHolderForAdapterPosition(viewModel.dates.value!!.size) as AdditionViewHolder
            view.clearData()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.LOAD_VOTES_HAS_ENDED) {
            viewModel.isLoading.value = false
            val view = binding.datesRecyclerView.findViewHolderForAdapterPosition(viewModel.dates.value!!.size) as AdditionViewHolder
            view.clearData(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        eventId = arguments!!.getLong(EXTRA_EVENT_ID)
        adapterPosition = arguments!!.getInt(EXTRA_ADAPTER_POSITION)

        binding = DataBindingUtil.inflate(inflater, R.layout.dates_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(DatesDialogViewModel::class.java)

        val viewAdapter = DateEntryAdapter(eventId, AdapterClickListener { position ->
            val view = binding.datesRecyclerView.findViewHolderForAdapterPosition(position) as DateViewHolder
            if (viewModel.isLoading.value!!) view.setRadioButtonUnchecked()
            else {
                view.showVoteCreateAnimation()
                viewModel.addVote(view.dateId)
            }
        })

        if (viewModel.dates.value == null || viewModel.dates.value!!.isEmpty())
            viewAdapter.addLoadingAndAddition()

        viewModel.dates.observe(this, Observer {
            viewAdapter.addAdditionAndSubmitList(it)
        })

        (binding.datesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.datesRecyclerView.adapter = viewAdapter
        binding.datesRecyclerView.layoutManager = LinearLayoutManager(context)

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (viewModel.votesChanged.value!! && viewModel.dates.value!!.isNotEmpty())
            EventBus.getDefault().post(UpdateEventRequest(eventId, adapterPosition))
    }

    companion object {
        private const val EXTRA_EVENT_ID = "eid"
        private const val EXTRA_ADAPTER_POSITION = "aps"

        fun newInstance(eventId: Long, adapterPosition: Int): DatesDialogFragment {
            val dialogFragment = DatesDialogFragment()
            val args = Bundle().apply {
                putLong(EXTRA_EVENT_ID, eventId)
                putInt(EXTRA_ADAPTER_POSITION, adapterPosition)
            }
            dialogFragment.arguments = args
            return dialogFragment
        }
    }
}
