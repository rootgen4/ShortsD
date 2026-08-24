package com.kingreels.shortsd

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var btnSelectFolder: Button

    private val videoList = mutableListOf<Uri>()

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val treeUri = result.data?.data ?: return@registerForActivityResult

                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                saveFolderUri(treeUri)
                loadVideosFromFolder(treeUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnSelectFolder = findViewById(R.id.btnSelectFolder)

        btnSelectFolder.setOnClickListener {
            openFolderPicker()
        }

        val savedUri = getSavedFolderUri()
        if (savedUri != null) {
            loadVideosFromFolder(savedUri)
        }
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        folderPickerLauncher.launch(intent)
    }

    private fun saveFolderUri(uri: Uri) {
        getSharedPreferences("shortsd_prefs", MODE_PRIVATE)
            .edit()
            .putString("folder_uri", uri.toString())
            .apply()
    }

    private fun getSavedFolderUri(): Uri? {
        val uriString = getSharedPreferences("shortsd_prefs", MODE_PRIVATE)
            .getString("folder_uri", null) ?: return null
        return Uri.parse(uriString)
    }

    private fun loadVideosFromFolder(treeUri: Uri) {
        videoList.clear()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idIndex)
                val mime = cursor.getString(mimeIndex)

                if (mime != null && mime.startsWith("video/")) {
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    videoList.add(docUri)
                }
            }
        }

        if (videoList.isNotEmpty()) {
            emptyStateLayout.visibility = LinearLayout.GONE
            viewPager.visibility = ViewPager2.VISIBLE

            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

            // Agle aur pichle 2 pages ke players pehle se taiyar (preload/buffer) rahenge
            viewPager.offscreenPageLimit = 2

            viewPager.adapter = ReelsAdapter(this, videoList)

            setupPageChangeCallback()

            // Pehla video manually play karo (onPageSelected pehli dafa fire nahi hota)
            viewPager.post {
                playVisiblePage(viewPager.currentItem)
            }
        } else {
            emptyStateLayout.visibility = LinearLayout.VISIBLE
            viewPager.visibility = ViewPager2.GONE
        }
    }

    private fun setupPageChangeCallback() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                playVisiblePage(position)
            }
        })
    }

    // Current page play karo, baqi sab (preloaded) pause rakho
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
}
