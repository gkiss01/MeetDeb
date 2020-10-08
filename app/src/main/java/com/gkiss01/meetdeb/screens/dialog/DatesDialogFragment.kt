package com.gkiss01.meetdeb.screens.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.DatePickerViewHolder
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.gkiss01.meetdeb.data.fastadapter.Date
import com.gkiss01.meetdeb.data.fastadapter.DatePickerItem
import com.gkiss01.meetdeb.data.isAdmin
import com.gkiss01.meetdeb.network.Status
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.utils.setNavigationResult
import com.gkiss01.meetdeb.viewmodels.DatesViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.android.synthetic.main.fragment_dates.*
import kotlinx.android.synthetic.main.item_date.view.*
import kotlinx.android.synthetic.main.item_date_picker.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.OffsetDateTime

class DatesDialogFragment : DialogFragment() {
    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: DatesViewModel by viewModel { parametersOf(viewModelActivityKoin.getBasic()) }

    private val itemAdapter = ItemAdapter<Date>()
    private val headerAdapter = ItemAdapter<ProgressItem>()
    private val footerAdapter = ItemAdapter<DatePickerItem>()
    private val fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter, footerAdapter))

    private val safeArgs: DatesDialogFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_dates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!viewModelKoin.isEventInitialized()) {
            viewModelKoin.event = safeArgs.event
            viewModelKoin.getDates()
        }

        if (viewModelActivityKoin.activeUser.value?.data?.isAdmin() == false) fastAdapter.attachDefaultListeners = false
        val layoutManager = LinearLayoutManager(context)
        df_datesRecyclerView.apply {
            adapter = fastAdapter
            this.layoutManager = layoutManager
            itemAnimator = AlphaInAnimator()

            setItemViewCacheSize(12)
        }

        viewModelKoin.dates.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    FastAdapterDiffUtil[itemAdapter] = it.data!!
                    headerAdapter.clear()
                    val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(layoutManager.itemCount - 1) as? DatePickerViewHolder
                    itemView?.clearAnimation(true)
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    headerAdapter.clear()
                    val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(layoutManager.itemCount - 1) as? DatePickerViewHolder
                    itemView?.clearAnimation(false)

                    if (viewModelKoin.isLoadingActive()) {
                        fastAdapter.notifyAdapterDataSetChanged()
                    }
                }
                Status.LOADING -> {
                    Log.d("MeetDebLog_DatesDialogFragment", "Dates are loading...")
                    headerAdapter.clear()
                    headerAdapter.add(ProgressItem())
                }
                else -> {}
            }
        })

        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        footerAdapter.add(DatePickerItem())
        footerAdapter.fastAdapter!!.addClickListener({ vh: DatePickerViewHolder -> vh.itemView.dlp_createButton }) { _, position, _, item ->
            val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(position) as? DatePickerViewHolder

            if (item.offsetDateTime.isBefore(OffsetDateTime.now()))
                itemView?.setError(getString(R.string.future_date_required))
            else {
                itemView?.setError(null)
                itemView?.showAnimation()

                viewModelKoin.createDate(item.offsetDateTime)
            }
        }

        itemAdapter.fastAdapter!!.addClickListener({ vh: DateViewHolder -> vh.itemView.dli_voteButton }) { _, position, _, item ->
            val itemView = df_datesRecyclerView.findViewHolderForAdapterPosition(position) as? DateViewHolder

            if (viewModelKoin.isLoadingActive()) itemView?.setUnchecked()
            else if (!item.accepted) {
                itemView?.showAnimation()

                viewModelKoin.changeVote(item.id)
            }
        }

        if (viewModelActivityKoin.activeUser.value?.data?.isAdmin() == true) {
            itemAdapter.fastAdapter!!.onLongClickListener = { itemView, _, item, _ ->
                PopupMenu(context, itemView).apply {
                    menu.add(0, R.id.delete, 0, R.string.event_more_delete)

                    setOnMenuItemClickListener {
                        if (it.itemId == R.id.delete) viewModelKoin.deleteDate(item.id)
                        true
                    }
                    show()
                }
                false
            }
        }
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

    override fun onDismiss(dialog: DialogInterface) {
        viewModelKoin.dates.value?.data?.let { dates ->
            if ((dates.any { it.accepted } && !viewModelKoin.event.voted) ||
                (!dates.any { it.accepted } && viewModelKoin.event.voted)) {
                setNavigationResult("eventId", viewModelKoin.event.id)
            }
        }
        super.onDismiss(dialog)
    }
}
