package com.gkiss01.meetdeb.screens.viewholders

import android.graphics.Color
import android.view.View
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.local.DatePickerItem
import com.gkiss01.meetdeb.databinding.ItemDatePickerBinding
import com.gkiss01.meetdeb.utils.format
import com.mikepenz.fastadapter.FastAdapter
import org.threeten.bp.OffsetDateTime

class DatePickerViewHolder(view: View): FastAdapter.ViewHolder<DatePickerItem>(view) {
    val binding = ItemDatePickerBinding.bind(view)
    private var expanded = false

    override fun bindView(item: DatePickerItem, payloads: List<Any>) {
        expanded = false
        binding.subLayout.visibility = View.GONE
        updateSelectedDate(item.offsetDateTime)
    }

    override fun unbindView(item: DatePickerItem) {
        expanded = false
        binding.subLayout.visibility = View.GONE
        binding.dateLabel.text = null
    }

    fun updateSelectedDate(offsetDateTime: OffsetDateTime) {
        binding.dateLabel.text = offsetDateTime.format()
    }

    fun setError(error: String?) {
        binding.dateLabel.error = error
    }

    fun closeOrExpand() {
        expanded = !expanded
        binding.subLayout.visibility = if (expanded) View.VISIBLE else View.GONE
        binding.downArrow.animate().setDuration(200).rotation(if (expanded) 180F else 0F)
    }

    fun manageAnimation(show: Boolean) {
        if (show) showAnimation() else hideAnimation()
    }

    private fun showAnimation() {
        binding.createButton.showProgress {
            buttonTextRes = R.string.date_create_waiting
            progressColor = Color.WHITE
        }
    }

    private fun hideAnimation() {
        binding.createButton.hideProgress(R.string.date_create_button)
    }

    fun close() {
        expanded = true
        closeOrExpand()
    }

    fun expand() {
        expanded = false
        closeOrExpand()
    }
}