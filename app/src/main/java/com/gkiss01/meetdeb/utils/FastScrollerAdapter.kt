package com.gkiss01.meetdeb.utils

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gkiss01.meetdeb.data.fastadapter.Event
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import org.threeten.bp.format.DateTimeFormatter

class FastScrollerAdapter<Item : GenericItem> : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    override fun getSectionName(position: Int): String {
        val event = fastAdapter?.getItem(position) as? Event
        return event?.date?.format(DateTimeFormatter.ofPattern("yyyy MMM dd")) ?: ""
    }

    // REQUIRED CODE FOR WRAPPING

    var fastAdapter: FastAdapter<Item>? = null
        private set

    fun wrap(fastAdapter: FastAdapter<Item>): FastScrollerAdapter<Item> {
        this.fastAdapter = fastAdapter
        return this
    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
        fastAdapter?.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
        fastAdapter?.unregisterAdapterDataObserver(observer)
    }

    override fun getItemViewType(position: Int): Int {
        return fastAdapter?.getItemViewType(position) ?: 0
    }

    override fun getItemId(position: Int): Long {
        return fastAdapter?.getItemId(position) ?: 0
    }

    override fun getItemCount(): Int {
        return fastAdapter?.itemCount ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val fastAdapter = this.fastAdapter ?: throw RuntimeException("A adapter needs to be wrapped")
        return fastAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        fastAdapter?.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        fastAdapter?.onBindViewHolder(holder, position, payloads)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        fastAdapter?.setHasStableIds(hasStableIds)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        fastAdapter?.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return fastAdapter?.onFailedToRecycleView(holder) ?: false
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        fastAdapter?.onViewDetachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        fastAdapter?.onViewAttachedToWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        fastAdapter?.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        fastAdapter?.onDetachedFromRecyclerView(recyclerView)
    }
}