package com.gkiss01.meetdeb.screens.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.data.remote.response.Participant
import com.gkiss01.meetdeb.databinding.FragmentParticipantsBinding
import com.gkiss01.meetdeb.utils.observeEvent
import com.gkiss01.meetdeb.viewmodels.ParticipantsViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class ParticipantsDialogFragment : DialogFragment() {
    private var _binding: FragmentParticipantsBinding? = null
    private val binding get() = _binding!!

    private val viewModelKoin: ParticipantsViewModel by viewModel()

    private val safeArgs: ParticipantsDialogFragmentArgs by navArgs()

    private val itemAdapter = ItemAdapter<Participant>()
    private val headerAdapter = ItemAdapter<ProgressItem>()
    private val fastAdapter = FastAdapter.with(listOf(headerAdapter, itemAdapter)).apply { attachDefaultListeners = false }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentParticipantsBinding.bind(view)

        if (!viewModelKoin.isEventInitialized()) {
            viewModelKoin.event = safeArgs.event
            viewModelKoin.getParticipants()
        }

        binding.recyclerView.apply {
            adapter = fastAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null

            setItemViewCacheSize(20)
        }

        // Toast üzenet
        viewModelKoin.toastEvent.observeEvent(viewLifecycleOwner) {
            when (it) {
                is Int -> Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // Header animáció kezelése
        viewModelKoin.headerCurrentlyNeeded.observe(viewLifecycleOwner) {
            headerAdapter.clear()
            if (it) headerAdapter.add(ProgressItem())
        }

        // Résztvevők lista újratöltése
        viewModelKoin.participants.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    FastAdapterDiffUtil.calculateDiff(itemAdapter, it)
                }
                FastAdapterDiffUtil[itemAdapter] = result
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}
