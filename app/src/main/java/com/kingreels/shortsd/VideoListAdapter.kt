package com.kingreels.shortsd

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoListAdapter(
    private val context: Context,
    private val videos: List<Uri>,
    private val onClick: (position: Int) -> Unit
) : RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoName: TextView = itemView.findViewById(R.id.videoName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val uri = videos[position]
        holder.videoName.text = VideoUtils.getFileName(context, uri)
        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = videos.size
}
