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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.AdditionViewHolder
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.gkiss01.meetdeb.data.Date
import com.gkiss01.meetdeb.data.DateList
import com.gkiss01.meetdeb.data.UpdateEventRequest
import com.gkiss01.meetdeb.network.ErrorCodes
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.android.synthetic.main.dates_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DatesDialogFragment : DialogFragment() {
    private val viewModel: DatesDialogViewModel by viewModels()

    private val itemAdapter = ItemAdapter<Date>()
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
    fun onDatesReceived(dates: DateList) {
        viewModel.setDates(dates.dates)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.DATE_ALREADY_CREATED) {
            val position = df_datesRecyclerView.layoutManager!!.childCount
            if (position == 0) return this.dismiss()

            val view = df_datesRecyclerView.findViewHolderForAdapterPosition(df_datesRecyclerView.layoutManager!!.childCount - 1) as AdditionViewHolder
            if (view.isProgressActive()) view.clearData()
            else if (!viewModel.isLoading) this.dismiss()
        }
        if (errorCode == ErrorCodes.UNKNOWN && viewModel.isLoading) {
            fastAdapter.notifyAdapterDataSetChanged()
            viewModel.isLoading = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dates_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.eventId = arguments!!.getLong(EXTRA_EVENT_ID)

        fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter))
        fastAdapter.attachDefaultListeners = false
        df_datesRecyclerView.adapter = fastAdapter

        df_datesRecyclerView.layoutManager = LinearLayoutManager(context)

        df_datesRecyclerView.setItemViewCacheSize(20)
        df_datesRecyclerView.itemAnimator = null

        fastAdapter.addEventHook(object : ClickEventHook<Date>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return if (viewHolder is DateViewHolder) viewHolder.voteButton else null
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<Date>, item: Date) {
                val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(position) as DateViewHolder

                if (viewModel.isLoading)
                    itemView.setRadioButtonUnchecked()
                else if (!item.accepted) {
                    viewModel.addVote(itemView.dateId)
                    itemView.showVoteCreateAnimation()
                }
            }
        })

        if (viewModel.dates.value == null || viewModel.dates.value!!.isEmpty()) {
            headerAdapter.clear()
            headerAdapter.add(ProgressItem())
        }

        viewModel.dates.observe(viewLifecycleOwner, Observer {
            FastAdapterDiffUtil[itemAdapter] = it
            if (viewModel.isLoading) viewModel.votesChanged = true
            viewModel.isLoading = false // félős
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (viewModel.votesChanged && viewModel.dates.value!!.isNotEmpty())
            EventBus.getDefault().post(UpdateEventRequest(viewModel.eventId))
    }

    companion object {
        private const val EXTRA_EVENT_ID = "eid"

        fun newInstance(eventId: Long): DatesDialogFragment {
            val dialogFragment = DatesDialogFragment()
            val args = Bundle().apply {
                putLong(EXTRA_EVENT_ID, eventId)
            }
            dialogFragment.arguments = args
            return dialogFragment
        }
    }
}
