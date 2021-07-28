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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.ActivityViewModel
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.local.DatePickerItem
import com.gkiss01.meetdeb.data.remote.response.Date
import com.gkiss01.meetdeb.data.remote.response.isAdmin
import com.gkiss01.meetdeb.databinding.FragmentDatesBinding
import com.gkiss01.meetdeb.screens.viewholders.DatePickerViewHolder
import com.gkiss01.meetdeb.screens.viewholders.DateViewHolder
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.utils.setNavigationResult
import com.gkiss01.meetdeb.viewmodels.DatesViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.OnBindViewHolderListenerImpl
import com.mikepenz.fastadapter.listeners.addClickListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import com.mikepenz.itemanimators.AlphaInAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.OffsetDateTime

class DatesDialogFragment : DialogFragment() {
    private var _binding: FragmentDatesBinding? = null
    private val binding get() = _binding!!

    private val viewModelActivityKoin: ActivityViewModel by sharedViewModel()
    private val viewModelKoin: DatesViewModel by viewModel()

    private val safeArgs: DatesDialogFragmentArgs by navArgs()

    private val itemAdapter = ItemAdapter<Date>()
    private val headerAdapter = ItemAdapter<ProgressItem>()
    private val footerAdapter = ItemAdapter<DatePickerItem>()
    private val fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter, footerAdapter))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_dates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDatesBinding.bind(view)

        if (!viewModelKoin.isEventInitialized()) {
            viewModelKoin.event = safeArgs.event
            viewModelKoin.getDates()
        }

        viewModelKoin.itemCurrentlyAdding.value?.let {
            footerAdapter.add(DatePickerItem(it))
        } ?: footerAdapter.add(DatePickerItem())

        if (viewModelActivityKoin.activeUser.value?.data?.isAdmin() == false) fastAdapter.attachDefaultListeners = false
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.apply {
            adapter = fastAdapter
            this.layoutManager = layoutManager
            itemAnimator = AlphaInAnimator()

            setItemViewCacheSize(12)
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Adott lista elem befrissítése
        viewModelKoin.updateItemEvent.observeEvent(viewLifecycleOwner) {
            fastAdapter.notifyAdapterItemChanged(itemAdapter.getAdapterPosition(it))
        }

        // Időpont gomb animációk kezelése
        viewModelKoin.itemCurrentlyUpdating.observe(viewLifecycleOwner) {
            it?.let { item ->
                getDateViewHolderByPosition(itemAdapter.getAdapterPosition(item))?.let { holder ->
                    holder.setChecked()
                    holder.showAnimation()
                }
            }
        }

        fastAdapter.onBindViewHolderListener = object : OnBindViewHolderListenerImpl<GenericItem>() {
            override fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder, position: Int) {
                viewModelKoin.itemCurrentlyUpdating.value?.let { item ->
                    (viewHolder as? DateViewHolder)?.let { holder ->
                        if (holder.date.id == item) {
                            holder.setChecked()
                            holder.showAnimation()
                        }
                    }
                }
                viewModelKoin.itemCurrentlyAdding.value?.let {
                    (viewHolder as? DatePickerViewHolder)?.let { holder ->
                        holder.expand()
                        holder.manageAnimation(true)
                    }
                }
            }
        }

        // Header animáció kezelése
        viewModelKoin.headerCurrentlyNeeded.observe(viewLifecycleOwner) {
            headerAdapter.clear()
            if (it) headerAdapter.add(ProgressItem())
        }

        // Footer animáció kezelése
        viewModelKoin.itemCurrentlyAdding.observe(viewLifecycleOwner) {
            getDatePickerViewHolderByPosition(layoutManager.itemCount - 1)?.manageAnimation(it != null)
        }

        viewModelKoin.collapseFooter.observeEvent(viewLifecycleOwner) {
            getDatePickerViewHolderByPosition(layoutManager.itemCount - 1)?.close()
        }

        // Időpont lista újratöltése
        viewModelKoin.dates.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    FastAdapterDiffUtil.calculateDiff(itemAdapter, it)
                }
                FastAdapterDiffUtil[itemAdapter] = result
            }
        }

        itemAdapter.fastAdapter?.addClickListener({ vh: DateViewHolder -> vh.binding.voteButton }) { _, _, _, item ->
            if (!item.accepted) viewModelKoin.changeVote(item.id)
        }

        footerAdapter.fastAdapter?.addClickListener({ vh: DatePickerViewHolder -> vh.binding.createButton }) { _, position, _, item ->
            val itemView = getDatePickerViewHolderByPosition(position)

            if (item.offsetDateTime.isBefore(OffsetDateTime.now()))
                itemView?.setError(getString(R.string.future_date_required))
            else {
                itemView?.setError(null)
                viewModelKoin.createDate(item.offsetDateTime)
            }
        }

        if (viewModelActivityKoin.activeUser.value?.data?.isAdmin() == true) {
            itemAdapter.fastAdapter?.onLongClickListener = { itemView, _, item, _ ->
                createMoreActionMenu(itemView, item)
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDateViewHolderByPosition(position: Int) = binding.recyclerView.findViewHolderForAdapterPosition(position) as? DateViewHolder
    private fun getDatePickerViewHolderByPosition(position: Int) = binding.recyclerView.findViewHolderForAdapterPosition(position) as? DatePickerViewHolder

    private fun createMoreActionMenu(view: View, date: Date) {
        viewModelActivityKoin.activeUser.value?.data?.let {
            PopupMenu(context, view).apply {
                menu.add(0, R.id.delete, 0, R.string.event_more_delete)

                setOnMenuItemClickListener {
                    if (it.itemId == R.id.delete) viewModelKoin.deleteDate(date.id)
                    true
                }
                show()
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
        viewModelKoin.dates.value?.let { dates ->
            if ((dates.any { it.accepted } && !viewModelKoin.event.voted) ||
                (!dates.any { it.accepted } && viewModelKoin.event.voted)) {
                setNavigationResult("eventId", viewModelKoin.event.id)
            }
        }
        super.onDismiss(dialog)
    }
}
