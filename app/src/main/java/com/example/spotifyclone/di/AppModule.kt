package com.example.spotifyclone.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.spotifyclone.R
import com.example.spotifyclone.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @InstallIn tells dagger in which component we have to install our module and which corresponding classes can use the
 * dependencies we have defined in this module class.
 * Component  lifetime = lifetime of the dependencies we have inside this module.

 *
 * SingletonComponent lifetime = Application lifetime. So the dependencies we have in this module will last till application lifetime.
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Single instance it will create.Everytime we request Glide object we get the same instance. */
    @Singleton
    @Provides
    fun provideGlideInstance(@ApplicationContext context: Context) =
        Glide.with(context).applyDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
        )

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)
}