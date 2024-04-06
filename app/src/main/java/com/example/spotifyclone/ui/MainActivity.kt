 package com.example.spotifyclone.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ActivityMainBinding
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

 @AndroidEntryPoint
 class MainActivity : AppCompatActivity() {

     val mainViewModel : MainViewModel by viewModels()
     private lateinit var binding: ActivityMainBinding
     private var currentPlayingSong: Song? = null
     private var playbackState: PlaybackStateCompat? = null

     @Inject lateinit var glide: RequestManager
     @Inject lateinit var swipeSongAdapter: SwipeSongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        subscribeToObservers()
        registerOnPageChangeCallback()
        onClickEvent()
        binding.vpSong.adapter = swipeSongAdapter
    }

     private fun switchViewPagerToCurrentSong(song: Song){
         val indexCur = swipeSongAdapter.songs.indexOf(song)
         if(indexCur != -1){
             binding.vpSong.currentItem = indexCur
             currentPlayingSong = song
         }
     }

     private fun showBottomBar(isVisible: Int){
         binding.vpSong.visibility = isVisible
         binding.ivPlayPause.visibility = isVisible
         binding.ivCurSongImage.visibility = isVisible
     }

     private fun onClickEvent(){
         binding.ivPlayPause.setOnClickListener {
             currentPlayingSong?.let {
                 mainViewModel.playOrToggleSong(it, true)
             }
         }

         findNavController(R.id.nav_host_fragment)
             .addOnDestinationChangedListener { _, destination, _ ->
                 when(destination.id){
                     R.id.songFragment -> showBottomBar(View.GONE)
                     R.id.homeFragment -> showBottomBar(View.VISIBLE)
                     else -> showBottomBar(View.VISIBLE)
                 }
             }

         swipeSongAdapter.setOnclickListener {
             findNavController(R.id.nav_host_fragment).navigate(R.id.global_action_to_song_fragment)
         }
     }

     private fun registerOnPageChangeCallback(){
         binding.vpSong.registerOnPageChangeCallback(object : OnPageChangeCallback(){
             override fun onPageSelected(position: Int) {
                 if(playbackState?.isPlaying == true){
                     mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                 }else{
                     currentPlayingSong = swipeSongAdapter.songs[position]
                 }
             }
         })
     }

     private fun subscribeToObservers(){
         mainViewModel.mediaItems.observe(this){result->
             result?.let {
                 when(result.status){ 
                     Status.SUCCESS -> {
                         if(!result.data.isNullOrEmpty()){
                             glide.load((currentPlayingSong ?: result.data[0]).imageUrl).into(binding.ivCurSongImage)
                             swipeSongAdapter.songs = result.data
                             switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
                         }
                     }
                     Status.ERROR -> Unit
                     Status.LOADING -> Unit
                 }
             }

         }

         mainViewModel.curPlayingSong.observe(this){
             if(it == null) return@observe

             currentPlayingSong = it.toSong()
             glide.load(currentPlayingSong?.imageUrl).into(binding.ivCurSongImage)
             switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
         }

         mainViewModel.playbackState.observe(this){
             playbackState = it
             binding.ivPlayPause.setImageResource(
                 if(it?.isPlaying == true) R.drawable.ic_pause  else R.drawable.ic_play
             )
         }

         mainViewModel.isConnected.observe(this){
             it.getContentIfNotHandled()?.let { result->
                 when(result.status){

                     Status.ERROR -> Snackbar.make(binding.rootLayout,
                         result.message ?: "An unknown error occurred",
                         Snackbar.LENGTH_LONG).show()

                     else -> Unit
                 }
             }
         }

         mainViewModel.networkError.observe(this){
             it.getContentIfNotHandled()?.let { result->
                 when(result.status){

                     Status.ERROR -> Snackbar.make(binding.rootLayout,
                         result.message ?: "An unknown error occurred",
                         Snackbar.LENGTH_LONG).show()

                     else -> Unit
                 }
             }
         }

     }

}