package com.gkiss01.meetdeb.screens

import android.app.Dialog
import android.os.Bundle
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
import com.gkiss01.meetdeb.data.Date
import com.gkiss01.meetdeb.databinding.DatesFragmentBinding

class DatesDialogFragment : DialogFragment() {

    private lateinit var binding: DatesFragmentBinding
    private lateinit var viewModel: DatesDialogViewModel
    private var dates = emptyList<Date>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dates_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(DatesDialogViewModel::class.java)

        if (viewModel.dates.value == null || viewModel.dates.value!!.isEmpty())
            viewModel.dates.value = dates

        val viewAdapter = DateEntryAdapter(AdapterClickListener { position ->
            val view = binding.datesRecyclerView.findViewHolderForAdapterPosition(position) as DateEntryAdapter.DateViewHolder
        })

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

    companion object {
        fun builder(dates: List<Date>): DatesDialogFragment {
            val datesDialogFragment = DatesDialogFragment()
            datesDialogFragment.dates = dates
            return datesDialogFragment
        }
    }
}
