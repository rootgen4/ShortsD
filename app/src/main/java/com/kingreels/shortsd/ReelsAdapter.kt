package com.kingreels.shortsd

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView

class ReelsAdapter(
    private val context: Context,
    private val videoList: List<Uri>,
    private val onVideoEnded: (position: Int) -> Unit
) : RecyclerView.Adapter<ReelsAdapter.ReelViewHolder>() {

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val seekIndicator: TextView = itemView.findViewById(R.id.seekIndicator)
        val speedIndicator: TextView = itemView.findViewById(R.id.speedIndicator)
        var exoPlayer: ExoPlayer? = null

        private var currentUri: Uri? = null
        private val handler = Handler(Looper.getMainLooper())
        private var isHolding = false
        private var longPressRunnable: Runnable? = null
        private var progressRunnable: Runnable? = null

        private val prefs by lazy {
            context.getSharedPreferences("shortsd_positions", Context.MODE_PRIVATE)
        }

        fun bind(uri: Uri) {
            currentUri = uri

            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                playerView.player = player
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.prepare()
                player.playWhenReady = false

                // Saved position se resume karo (agar hai)
                val savedPosition = prefs.getLong(uri.toString(), 0L)
                if (savedPosition > 0) {
                    player.seekTo(savedPosition)
                }

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            // Video khatam hui -> uski saved position clear kar do
                            prefs.edit().remove(uri.toString()).apply()
                            onVideoEnded(bindingAdapterPosition)
                        }
                    }
                })
            }

            startProgressUpdates()
            setupTouchControl()
        }

        fun play() {
            exoPlayer?.playWhenReady = true
        }

        fun pause() {
            exoPlayer?.playWhenReady = false
            savePosition()
        }

        private fun savePosition() {
            val player = exoPlayer ?: return
            val uri = currentUri ?: return
            if (player.duration > 0 && player.currentPosition < player.duration - 1000) {
                prefs.edit().putLong(uri.toString(), player.currentPosition).apply()
            }
        }

        private fun startProgressUpdates() {
            progressRunnable = object : Runnable {
                override fun run() {
                    exoPlayer?.let { player ->
                        val duration = player.duration
                        if (duration > 0) {
                            val progress = (player.currentPosition * 1000 / duration).toInt()
                            progressBar.progress = progress
                        }
                    }
                    handler.postDelayed(this, 100)
                }
            }
            handler.post(progressRunnable!!)
        }

        private fun setupTouchControl() {
            playerView.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isHolding = false
                        longPressRunnable = Runnable {
                            isHolding = true
                            exoPlayer?.playbackParameters = PlaybackParameters(2f)
                            speedIndicator.visibility = View.VISIBLE
                        }
                        handler.postDelayed(longPressRunnable!!, 200)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        longPressRunnable?.let { handler.removeCallbacks(it) }

                        if (isHolding) {
                            // Hold khatam -> wapis normal speed
                            exoPlayer?.playbackParameters = PlaybackParameters(1f)
                            speedIndicator.visibility = View.GONE
                        } else {
                            // Simple tap -> position check karo (left/right/center)
                            val width = view.width
                            val x = event.x
                            when {
                                x < width / 3f -> seekBackward()
                                x > width * 2f / 3f -> seekForward()
                                else -> togglePlayPause()
                            }
                        }
                        isHolding = false
                        true
                    }
                    else -> false
                }
            }
        }

        private fun togglePlayPause() {
            exoPlayer?.let {
                it.playWhenReady = !it.playWhenReady
            }
        }

        private fun seekForward() {
            exoPlayer?.let {
                val newPos = (it.currentPosition + 10000).coerceAtMost(it.duration)
                it.seekTo(newPos)
            }
            showSeekFeedback("+10s »")
        }

        private fun seekBackward() {
            exoPlayer?.let {
                val newPos = (it.currentPosition - 10000).coerceAtLeast(0)
                it.seekTo(newPos)
            }
            showSeekFeedback("« -10s")
        }

        private fun showSeekFeedback(text: String) {
            seekIndicator.text = text
            seekIndicator.visibility = View.VISIBLE
            handler.postDelayed({ seekIndicator.visibility = View.GONE }, 500)
        }

        fun releasePlayer() {
            savePosition()
            progressRunnable?.let { handler.removeCallbacks(it) }
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
