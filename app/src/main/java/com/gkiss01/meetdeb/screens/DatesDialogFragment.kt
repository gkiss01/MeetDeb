package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.adapter.AdapterClickListener
import com.gkiss01.meetdeb.adapter.DateEntryAdapter
import com.gkiss01.meetdeb.adapter.DateViewHolder
import com.gkiss01.meetdeb.data.DateList
import com.gkiss01.meetdeb.databinding.DatesFragmentBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DatesDialogFragment : DialogFragment() {

    private lateinit var binding: DatesFragmentBinding
    private lateinit var viewModel: DatesDialogViewModel

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDatesReceived(dates: DateList) {
        viewModel.dates.value = dates.dates
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dates_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(DatesDialogViewModel::class.java)

        val viewAdapter = DateEntryAdapter(AdapterClickListener { position ->
            val view = binding.datesRecyclerView.findViewHolderForAdapterPosition(position) as DateViewHolder
            Log.d("DatesDialogFragment", "Clicked on date with ID ${view.dateId}")
        })

        if (viewModel.dates.value == null || viewModel.dates.value!!.isEmpty())
            viewAdapter.addLoadingAndAddition()

        viewModel.dates.observe(this, Observer {
            viewAdapter.addAdditionAndSubmitList(it)
        })

        binding.datesRecyclerView.adapter = viewAdapter
        binding.datesRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
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
}
