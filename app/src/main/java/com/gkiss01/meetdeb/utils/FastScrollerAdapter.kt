package com.gkiss01.meetdeb.utils

import com.gkiss01.meetdeb.data.fastadapter.Event
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.IAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import org.threeten.bp.format.DateTimeFormatter

class FastScrollerAdapter<Item : GenericItem> : FastAdapter<Item>(), FastScrollRecyclerView.SectionedAdapter {
    override fun getSectionName(position: Int): String {
        val event = getItem(position) as? Event
        return event?.date?.format(DateTimeFormatter.ofPattern("yyyy MMM dd")) ?: ""
    }

    companion object {
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <Item : GenericItem> with(adapters: Collection<IAdapter<out Item>>?): FastScrollerAdapter<Item> {
            val fastAdapter = FastScrollerAdapter<Item>()
            val adapterArray = adapters as? Collection<IAdapter<Item>>
            adapterArray?.forEachIndexed { index, iAdapter ->
                fastAdapter.addAdapter(index, iAdapter)
            }
            return fastAdapter
        }
    }
}