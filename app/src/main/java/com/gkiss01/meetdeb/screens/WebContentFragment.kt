package com.gkiss01.meetdeb.screens

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.gkiss01.meetdeb.R
import com.gkiss01.meetdeb.databinding.WebContentFragmentBinding
import com.gkiss01.meetdeb.network.adapter.AlbumAdapter

class WebContentFragment : Fragment() {

    private lateinit var binding: WebContentFragmentBinding
    private lateinit var viewModel: WebContentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.web_content_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(WebContentViewModel::class.java)

        val viewAdapter = AlbumAdapter()
        viewModel.albums.observe(this, Observer { albums ->
            albums?.let { viewAdapter.submitList(it) }
        })

        binding.albumsRecyclerView.adapter = viewAdapter
        binding.albumsRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }
}
