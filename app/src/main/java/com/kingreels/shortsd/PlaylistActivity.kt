package com.kingreels.shortsd

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlaylistActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var folderUri: Uri
    private val videoList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        val folderUriString = intent.getStringExtra("folder_uri")
        if (folderUriString == null) {
            finish()
            return
        }
        folderUri = Uri.parse(folderUriString)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = VideoUtils.getFolderName(this, folderUri)

        recyclerView = findViewById(R.id.recyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadVideos()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadVideos() {
        videoList.clear()
        videoList.addAll(VideoUtils.listVideosInFolder(this, folderUri))

        if (videoList.isEmpty()) {
            emptyStateLayout.visibility = LinearLayout.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            emptyStateLayout.visibility = LinearLayout.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerView.adapter = VideoListAdapter(this, videoList) { position ->
                openReels(position)
            }
        }
    }

    private fun openReels(startPosition: Int) {
        val intent = Intent(this, ReelsActivity::class.java)
        intent.putExtra("folder_uri", folderUri.toString())
        intent.putExtra("start_position", startPosition)
        startActivity(intent)
    }
}
