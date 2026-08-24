package com.kingreels.shortsd

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kingreels.shortsd.VideoUtils

class FolderAdapter(
    private val context: Context,
    private val folders: List<Uri>,
    private val onClick: (Uri) -> Unit,
    private val onRemove: (Uri) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName: TextView = itemView.findViewById(R.id.folderName)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val uri = folders[position]
        holder.folderName.text = VideoUtils.getFolderName(context, uri)
        holder.itemView.setOnClickListener { onClick(uri) }
        holder.btnRemove.setOnClickListener { onRemove(uri) }
    }

    override fun getItemCount(): Int = folders.size
}
