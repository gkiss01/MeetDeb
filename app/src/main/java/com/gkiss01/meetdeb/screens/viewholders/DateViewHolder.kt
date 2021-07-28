package com.gkiss01.meetdeb.screens.viewholders

import android.graphics.Color
import android.view.View
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Date
import com.gkiss01.meetdeb.data.remote.response.Event
import com.gkiss01.meetdeb.databinding.ItemDateBinding
import com.gkiss01.meetdeb.utils.format
import com.gkiss01.meetdeb.viewmodels.ItemUpdating
import com.mikepenz.fastadapter.FastAdapter

class DateViewHolder(private val view: View): FastAdapter.ViewHolder<Date>(view) {
    val binding = ItemDateBinding.bind(view)

    override fun bindView(item: Date, payloads: List<Any>) {
        binding.dateLabel.text = item.date.format()
        binding.votesLabel.text = view.context.getString(R.string.event_votes, item.votes)
        binding.voteButton.isChecked = item.accepted

        payloads.filterIsInstance<ItemUpdating>().firstOrNull()?.let {
            if (item.id == it.second) manageAnimation(Event.UpdatingType.VOTE) else manageAnimation(Event.UpdatingType.NONE)
        } ?: manageAnimation(Event.UpdatingType.NONE)
    }

    override fun unbindView(item: Date) {
        binding.dateLabel.text = null
        binding.votesLabel.text = null
        binding.voteButton.isChecked = false
    }

    private fun manageAnimation(type: Event.UpdatingType) = when(type) {
        Event.UpdatingType.VOTE -> showVoteAnimation()
        Event.UpdatingType.NONE -> clearAnimations()
        else -> {}
    }

    private fun clearAnimations() {
        binding.voteButton.hideProgress()
    }

    private fun showVoteAnimation() {
        binding.voteButton.isChecked = true
        binding.voteButton.showProgress {
            progressColor = Color.parseColor("#485688")
        }
    }
}