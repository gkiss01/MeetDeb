package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.AdapterClickListener
import com.gkiss01.meetdeb.adapter.AdditionViewHolder
import com.gkiss01.meetdeb.adapter.DateEntryAdapter
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.gkiss01.meetdeb.data.DateList
import com.gkiss01.meetdeb.data.UpdateEventRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.network.NavigationCode
import kotlinx.android.synthetic.main.dates_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DatesDialogFragment : DialogFragment() {

    private lateinit var viewModel: DatesDialogViewModel
    private lateinit var viewAdapter: DateEntryAdapter

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
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.DATE_ALREADY_CREATED) {
            val position = df_datesRecyclerView.layoutManager!!.childCount
            if (position == 0) return this.dismiss()

            val view = df_datesRecyclerView.findViewHolderForAdapterPosition(df_datesRecyclerView.layoutManager!!.childCount - 1) as AdditionViewHolder
            if (view.isProgressActive()) view.clearData()
            else if (viewModel.isLoading.value == false) this.dismiss()
        }
        if (errorCode == ErrorCodes.UNKNOWN && viewModel.isLoading.value == true) {
            viewAdapter.notifyDataSetChanged()
            viewModel.isLoading.value = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationReceived(navigationCode: NavigationCode) {
        if (navigationCode == NavigationCode.LOAD_VOTES_HAS_ENDED) {
            if (viewModel.isLoading.value == true) viewModel.votesChanged.value = true
            viewModel.isLoading.value = false
            val view = df_datesRecyclerView.findViewHolderForAdapterPosition(viewModel.dates.value!!.size) as AdditionViewHolder
            view.clearData(true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dates_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(DatesDialogViewModel::class.java)

        eventId = arguments!!.getLong(EXTRA_EVENT_ID)
        adapterPosition = arguments!!.getInt(EXTRA_ADAPTER_POSITION)

        viewAdapter = DateEntryAdapter(eventId, AdapterClickListener { position ->
            val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(position) as DateViewHolder
            if (viewModel.isLoading.value!!) itemView.setRadioButtonUnchecked()
            else {
                itemView.showVoteCreateAnimation()
                viewModel.addVote(itemView.dateId)
            }
        })

        if (viewModel.dates.value == null || viewModel.dates.value!!.isEmpty())
            viewAdapter.addLoadingAndAddition()

        viewModel.dates.observe(viewLifecycleOwner, Observer {
            viewAdapter.addDatesAndAddition(it)
        })

        (df_datesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        df_datesRecyclerView.adapter = viewAdapter
        df_datesRecyclerView.layoutManager = LinearLayoutManager(context)
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
