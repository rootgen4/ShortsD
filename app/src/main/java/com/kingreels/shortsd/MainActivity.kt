package com.kingreels.shortsd

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var btnAddFolder: Button

    private val folderList = mutableListOf<Uri>()
    private lateinit var folderAdapter: FolderAdapter

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val treeUri = result.data?.data ?: return@registerForActivityResult
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                addFolder(treeUri)
                refreshFolderList()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnAddFolder = findViewById(R.id.btnAddFolder)

        recyclerView.layoutManager = LinearLayoutManager(this)
        folderAdapter = FolderAdapter(
            context = this,
            folders = folderList,
            onClick = { uri -> openPlaylist(uri) },
            onRemove = { uri ->
                removeFolder(uri)
                refreshFolderList()
            }
        )
        recyclerView.adapter = folderAdapter

        btnAddFolder.setOnClickListener {
            openFolderPicker()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFolderList()
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        folderPickerLauncher.launch(intent)
    }

    private fun openPlaylist(uri: Uri) {
        val intent = Intent(this, PlaylistActivity::class.java)
        intent.putExtra("folder_uri", uri.toString())
        startActivity(intent)
    }

    private fun addFolder(uri: Uri) {
        val prefs = getSharedPreferences("shortsd_prefs", MODE_PRIVATE)
        val current = prefs.getStringSet("folder_uris", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(uri.toString())
        prefs.edit().putStringSet("folder_uris", current).apply()
    }

    private fun removeFolder(uri: Uri) {
        val prefs = getSharedPreferences("shortsd_prefs", MODE_PRIVATE)
        val current = prefs.getStringSet("folder_uris", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(uri.toString())
        prefs.edit().putStringSet("folder_uris", current).apply()
    }

    private fun getSavedFolders(): List<Uri> {
        val prefs = getSharedPreferences("shortsd_prefs", MODE_PRIVATE)
        val stored = prefs.getStringSet("folder_uris", emptySet()) ?: emptySet()
        return stored.map { Uri.parse(it) }
    }

    private fun refreshFolderList() {
        folderList.clear()
        folderList.addAll(getSavedFolders())
        folderAdapter.notifyDataSetChanged()

        if (folderList.isEmpty()) {
            emptyStateLayout.visibility = LinearLayout.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            emptyStateLayout.visibility = LinearLayout.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
    }
}
