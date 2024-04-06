package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.FragmentSongBinding
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.MainActivity
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.example.spotifyclone.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment: Fragment() {

    @Inject  lateinit var glide: RequestManager
    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()
    private var curPlayingSong: Song? = null
    private lateinit var binding: FragmentSongBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = (activity as MainActivity).mainViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_song,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
    }

    private fun updateTitleAndSong(song: Song){
        binding.tvSongName.text = song.title
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let { result->
                when(result.status){
                    Status.SUCCESS -> {
                        result.data?.let {songs ->
                            if(curPlayingSong == null && songs.isNotEmpty()){
                                curPlayingSong = songs[0]
                                updateTitleAndSong(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }

            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            it?.let {
                curPlayingSong = it.toSong()
                updateTitleAndSong(curPlayingSong!!)
            }
        }
    }
}