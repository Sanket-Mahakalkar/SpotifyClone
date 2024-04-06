package com.example.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.example.spotifyclone.R
import com.google.android.material.textview.MaterialTextView
import javax.inject.Inject

class SwipeSongAdapter @Inject constructor() : BaseSongAdapter(R.layout.swipe_item) {


    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {

            findViewById<MaterialTextView>(R.id.tvPrimary).text = song.title
            setOnClickListener {
                onItemClickListener?.invoke(song)
            }
        }
    }
}