package com.example.spotifyclone.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.example.spotifyclone.data.entities.remote.MusicDatabase
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase) {

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()
    var songs = emptyList<MediaMetadataCompat>()

    private var state:State = State.STATE_CREATED
        set(value) {
            if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR){
                /**synchronized means if one thread is executing this block then other thread is not allowed
                 to enter this block unless the thread executing this block finished the job and get out of this block */
                synchronized(onReadyListeners){
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            }else{
                field = value
            }
        }

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = State.STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()

        /** Now we need to convert "Song" object to MediaMetadataCompat object which is require to our Service*/
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .build()
        }

        state = State.STATE_INITIALIZED
    }

    fun whenReady(action: (Boolean) -> Unit): Boolean{
        return if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING){
            onReadyListeners += action
            false
        }else{
            action(state == State.STATE_INITIALIZED)
            true
        }
    }

    /** We need to concatenate our list of songs so that if one song  over it should automatically play next song*/
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource{
        val concatenatingMediaSource =  ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }




    /** MediaItem can be item or album or playlist*/
    fun asMediaItem() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING, // we are about to start music downloading from firestore..
    STATE_INITIALIZED, // we successfully fetch song from firestore
    STATE_ERROR         // we failed to fetch song from firestore
}