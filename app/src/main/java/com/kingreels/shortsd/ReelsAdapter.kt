package com.kingreels.shortsd

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView

class ReelsAdapter(
    private val context: android.content.Context,
    private val videoList: List<Uri>
) : RecyclerView.Adapter<ReelsAdapter.ReelViewHolder>() {

    inner class ReelViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        var exoPlayer: ExoPlayer? = null

        private val handler = Handler(Looper.getMainLooper())
        private var isHolding = false
        private var longPressRunnable: Runnable? = null

        fun bind(uri: Uri) {
            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                playerView.player = player
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.prepare()
                player.playWhenReady = true
            }

            setupTouchControl()
        }

        private fun setupTouchControl() {
            playerView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isHolding = false
                        longPressRunnable = Runnable {
                            isHolding = true
                            exoPlayer?.playbackParameters = PlaybackParameters(2f)
                        }
                        handler.postDelayed(longPressRunnable!!, 200)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        longPressRunnable?.let { handler.removeCallbacks(it) }

                        if (isHolding) {
                            // Hold khatam -> wapis normal speed
                            exoPlayer?.playbackParameters = PlaybackParameters(1f)
                        } else {
                            // Ye simple tap tha -> pause/play toggle
                            exoPlayer?.let {
                                it.playWhenReady = !it.playWhenReady
                            }
                        }
                        isHolding = false
                        true
                    }
                    else -> false
                }
            }
        }

        fun releasePlayer() {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_reel, parent, false)
        return ReelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun getItemCount(): Int = videoList.size
}
