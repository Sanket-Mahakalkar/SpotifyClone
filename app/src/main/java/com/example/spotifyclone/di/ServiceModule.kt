package com.example.spotifyclone.di

import android.content.Context
import com.example.spotifyclone.data.entities.remote.MusicDatabase
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

/**
* ServiceComponent = dependencies will live till our service lifetime
*/

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    /** Audio Attributes saves some meta information about out player.
     * @ServiceScoped means Hilt will provide same instance of audio attribute
     * throughout the life of the corresponding service instance
     */

    @ServiceScoped
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()


    /** Its a player that will play our music */
    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ) = SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes,true)
        setHandleAudioBecomingNoisy(true)

    // It will pause our music player when it can be noisy for user. Eg when user
    // plug in earphone then it can be noisy for him. So user need to click to play again.

    }


    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context) =
        DefaultDataSourceFactory(context, Util.getUserAgent(context,"spotify app"))

    @ServiceScoped
    @Provides
    fun provideMusicDatabase() = MusicDatabase()

}