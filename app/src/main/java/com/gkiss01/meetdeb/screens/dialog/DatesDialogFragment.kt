package com.gkiss01.meetdeb.screens.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.MainActivity
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DatePickerViewHolder
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.gkiss01.meetdeb.data.DateList
import com.gkiss01.meetdeb.data.adapterrequest.DeleteDateRequest
import com.gkiss01.meetdeb.data.adapterrequest.UpdateEventRequest
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.DatePickerItem
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.gkiss01.meetdeb.network.ErrorCodes
import com.gkiss01.meetdeb.utils.isActiveUserAdmin
import com.gkiss01.meetdeb.viewmodels.DatesViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.android.synthetic.main.fragment_dates.*
import kotlinx.android.synthetic.main.item_date.view.*
import kotlinx.android.synthetic.main.item_date_picker.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.threeten.bp.OffsetDateTime

class DatesDialogFragment : DialogFragment() {
    private val viewModel: DatesViewModel by viewModels()

    private val itemAdapter = ItemAdapter<Date>()
    private val headerAdapter = ItemAdapter<ProgressItem>()
    private val footerAdapter = ItemAdapter<DatePickerItem>()
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
    fun onDeleteRequestReceived(request: DeleteDateRequest) {
        viewModel.deleteDate(request.dateId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorReceived(errorCode: ErrorCodes) {
        if (errorCode == ErrorCodes.UNKNOWN || errorCode == ErrorCodes.DATE_ALREADY_CREATED) {
            val position = df_datesRecyclerView.layoutManager!!.childCount
            if (position == 0) return this.dismiss()

            val view = df_datesRecyclerView.findViewHolderForAdapterPosition(position - 1) as DatePickerViewHolder
            if (view.isProgressActive()) view.clearAnimation()
            else if (!viewModel.isLoading) this.dismiss()
        }
        if (errorCode == ErrorCodes.UNKNOWN && viewModel.isLoading) {
            fastAdapter.notifyAdapterDataSetChanged()
            viewModel.isLoading = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_dates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.event = arguments!!.getSerializable("event") as Event

        fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter, footerAdapter))
        if (!isActiveUserAdmin()) fastAdapter.attachDefaultListeners = false
        df_datesRecyclerView.adapter = fastAdapter

        val layoutManager = LinearLayoutManager(context)
        df_datesRecyclerView.layoutManager = layoutManager

        df_datesRecyclerView.setItemViewCacheSize(12)
        df_datesRecyclerView.itemAnimator = AlphaInAnimator()

        itemAdapter.fastAdapter!!.onLongClickListener = { itemView, _, item, _ ->
            PopupMenu(context, itemView).apply {
                menu.add(0, R.id.delete, 0, R.string.event_more_delete)

                setOnMenuItemClickListener {
                    if (it.itemId == R.id.delete) MainActivity.instance.deleteDate(item.id)
                    true
                }
                show()
            }
            false
        }

        itemAdapter.fastAdapter!!.addClickListener({ vh: DateViewHolder -> vh.itemView.dli_voteButton }) { _, position, _, item ->
            val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(position) as DateViewHolder

            if (viewModel.isLoading)
                itemView.setUnchecked()
            else if (!item.accepted) {
                viewModel.createVote(itemView.dateId)
                itemView.showAnimation()
            }
        }

        footerAdapter.add(DatePickerItem())
        footerAdapter.fastAdapter!!.addClickListener({ vh: DatePickerViewHolder -> vh.itemView.dlp_createButton }) { _, position, _, item ->
            val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(position) as DatePickerViewHolder

            if (item.offsetDateTime.isBefore(OffsetDateTime.now()))
                itemView.setError("Jövőbeli dátumot adj meg!")
            else {
                MainActivity.instance.createDate(viewModel.event.id, item.offsetDateTime)

                itemView.setError(null)
                itemView.showAnimation()
            }
        }

        if (viewModel.dates.value == null || viewModel.dates.value!!.isEmpty()) {
            headerAdapter.clear()
            headerAdapter.add(ProgressItem())
        }

        viewModel.dates.observe(viewLifecycleOwner, Observer {
            FastAdapterDiffUtil[itemAdapter] = it
            viewModel.isLoading = false // félős
            headerAdapter.clear()

            val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(layoutManager.itemCount - 1) as? DatePickerViewHolder
            itemView?.clearAnimation(true)
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
        if ((viewModel.dates.value!!.any { it.accepted } && !viewModel.event.voted) ||
            (!viewModel.dates.value!!.any { it.accepted } && viewModel.event.voted))
            EventBus.getDefault().post(UpdateEventRequest(viewModel.event.id))
    }
}
