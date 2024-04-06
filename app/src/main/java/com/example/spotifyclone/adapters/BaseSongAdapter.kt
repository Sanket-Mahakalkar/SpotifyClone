package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.google.android.material.textview.MaterialTextView

 abstract class BaseSongAdapter(
    private val layout: Int
): RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>()  {

    /**
     * we will use Diffutil for recycler view. It works asynchronously.
     * Earlier we used to use notifyDataSetChanged and with this we refresh the whole recycler view
     * But diffUtil only refresh that element which gets affected like delete a element then nothing
     * happens to rest of the elements of List. Animation is also taken care by diffUtil class
     */
    protected val diffCallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            // this function will compare the primary key of old item and new item
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            // this function will compare the whole object
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

     protected abstract val differ: AsyncListDiffer<Song>

     var songs: List<Song>
         get() = differ.currentList
         set(value) = differ.submitList(value)

     protected var onItemClickListener: ((Song) -> Unit)? = null

     fun setOnclickListener(listener: (Song) -> Unit){
         onItemClickListener = listener
     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(layout,parent,false)
        )
    }

     override fun getItemCount(): Int {
         return songs.size
     }


    inner class SongViewHolder( itemView: View): RecyclerView.ViewHolder(itemView)
}