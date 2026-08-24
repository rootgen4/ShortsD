package com.kingreels.shortsd

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class ReelsActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private val videoList = mutableListOf<Uri>()
    private var isActivityVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reels)

        viewPager = findViewById(R.id.viewPager)

        val folderUriString = intent.getStringExtra("folder_uri")
        val startPosition = intent.getIntExtra("start_position", 0)

        if (folderUriString == null) {
            finish()
            return
        }

        val folderUri = Uri.parse(folderUriString)
        videoList.addAll(VideoUtils.listVideosInFolder(this, folderUri))

        if (videoList.isEmpty()) {
            finish()
            return
        }

        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = ReelsAdapter(this, videoList) { finishedPosition ->
            onVideoEnded(finishedPosition)
        }

        setupPageChangeCallback()

        viewPager.setCurrentItem(startPosition, false)
        viewPager.post {
            playVisiblePage(viewPager.currentItem)
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        if (videoList.isNotEmpty()) {
            playVisiblePage(viewPager.currentItem)
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
        pauseAllPages()
    }

    private fun setupPageChangeCallback() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isActivityVisible) {
                    playVisiblePage(position)
                }
            }
        })
    }

    private fun onVideoEnded(finishedPosition: Int) {
        if (viewPager.currentItem == finishedPosition && finishedPosition < videoList.size - 1) {
            viewPager.setCurrentItem(finishedPosition + 1, true)
        }
    }

    private fun playVisiblePage(selectedPosition: Int) {
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView ?: return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val childPosition = recyclerView.getChildAdapterPosition(child)
            val holder = recyclerView.getChildViewHolder(child) as? ReelsAdapter.ReelViewHolder
            if (childPosition == selectedPosition) {
                holder?.play()
            } else {
                holder?.pause()
            }
        }
    }

    private fun pauseAllPages() {
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView ?: return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child) as? ReelsAdapter.ReelViewHolder
            holder?.pause()
        }
    }
}
