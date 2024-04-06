package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SongAdapter
import com.example.spotifyclone.databinding.FragmentHomeBinding
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.MainActivity
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentHomeBinding

    @Inject lateinit var songAdapter : SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = (activity as MainActivity).mainViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToObservers()

        songAdapter.setOnclickListener {song->
            mainViewModel.playOrToggleSong(song)
        }
    }

    private fun setupRecyclerView() {
        binding.rvAllSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){ result ->
            when(result.status){
                Status.SUCCESS -> {
                    binding.allSongsProgressBar.isVisible = false
                    if(!result.data.isNullOrEmpty()){
                        songAdapter.songs = result.data
                    }
                }
                Status.LOADING -> {
                    binding.allSongsProgressBar.isVisible = true
                }
                Status.ERROR -> Unit
            }

        }
    }
}