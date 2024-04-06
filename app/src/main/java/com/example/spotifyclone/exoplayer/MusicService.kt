package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.example.spotifyclone.other.Constants
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * In our case we are going to use MediaBrowserServiceCompat() service instead of normal Service.
 * Music player app like spotify, do not just have list of songs but we can browse through playlist or albums.
 * Like a File Manager we can navigate to other other playlist or albums.
 * So for that we have onLoadChildren() which contains @param parentId that identifies specific albums or playlist
 * and load songs or other albums related to that.
 */

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource



    /** custom coroutine scope that will destroy all coroutines running inside this scope when service dies.
     * serviceScope is tight to lifecycle of service*/
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false
    private lateinit var musicNotificationManager: MusicNotificationManager
    private var currentPlayingSong: MediaMetadataCompat? = null
    private var isPlayerInitialized = false
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    companion object{
        var curSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        /** get activity intent for our notification. When user click on notification we want to open our activity */
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
           PendingIntent.getActivity(this,0,it, PendingIntent.FLAG_IMMUTABLE)
        }

        mediaSession = MediaSessionCompat(this, Constants.SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken
        musicNotificationManager = MusicNotificationManager(this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            curSongDuration = exoPlayer.duration
        }

        var musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            currentPlayingSong = it
            preparePlayer(firebaseMusicSource.songs,
                it,
                true)
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)
        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)

    }

    /** service need media description when song changes to show it in notification*/
    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession){
        // this function is called when song changes and it returns media description of currently playing song
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ){
        val currSongIndex = if(currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        serviceScope.launch {
            withContext(Dispatchers.Main){
                exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
                exoPlayer.seekTo(currSongIndex,0)
                exoPlayer.playWhenReady = playNow
            }
        }

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(Constants.MEDIA_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            Constants.MEDIA_ROOT_ID -> {
                var resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                    if(isInitialized){
                        result.sendResult(firebaseMusicSource.asMediaItem())
                        if(!isPlayerInitialized && !firebaseMusicSource.songs.isNullOrEmpty()){
                            preparePlayer(firebaseMusicSource.songs,firebaseMusicSource.songs[0],false)
                            isPlayerInitialized = true
                        }
                    }else{
                        mediaSession.sendSessionEvent(Constants.NETWORK_ERROR,null )
                        result.sendResult(null)
                    }
                }
                if(!resultsSent){
                    result.detach()
                }
            }
        }

    }
}