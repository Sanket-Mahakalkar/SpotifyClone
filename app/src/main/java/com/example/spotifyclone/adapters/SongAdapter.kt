package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.google.android.material.textview.MaterialTextView
import javax.inject.Inject

class SongAdapter @Inject constructor(private val glide: RequestManager): BaseSongAdapter(R.layout.list_item) {


    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            findViewById<MaterialTextView>(R.id.tvPrimary).text = song.title
            findViewById<MaterialTextView>(R.id.tvSecondary).text = song.title
            glide.load(song.imageUrl).into(
                findViewById(R.id.ivItemImage))
            setOnClickListener {
                onItemClickListener?.invoke(song)
            }
        }
    }
}