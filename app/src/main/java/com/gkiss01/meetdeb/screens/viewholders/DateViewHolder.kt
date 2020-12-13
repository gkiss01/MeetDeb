package com.gkiss01.meetdeb.screens.viewholders

import android.graphics.Color
import android.view.View
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Date
import com.gkiss01.meetdeb.databinding.ItemDateBinding
import com.gkiss01.meetdeb.utils.format
import com.mikepenz.fastadapter.FastAdapter

class DateViewHolder(private val view: View): FastAdapter.ViewHolder<Date>(view) {
    val binding = ItemDateBinding.bind(view)
    lateinit var date: Date

    override fun bindView(item: Date, payloads: List<Any>) {
        date = item
        binding.dateLabel.text = item.date.format()
        binding.votesLabel.text = view.context.getString(R.string.event_votes, item.votes)
        binding.voteButton.isChecked = item.accepted

        binding.voteButton.hideProgress()
    }

    override fun unbindView(item: Date) {
        binding.dateLabel.text = null
        binding.votesLabel.text = null
        binding.voteButton.isChecked = false
    }

    fun showAnimation() {
        binding.voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }

    fun setChecked() {
        binding.voteButton.isChecked = true
    }
}