package com.kingreels.shortsd

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

object VideoUtils {

    fun listVideosInFolder(context: Context, treeUri: Uri): List<Uri> {
        val videoList = mutableListOf<Uri>()
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri, DocumentsContract.getTreeDocumentId(treeUri)
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (cursor.moveToNext()) {
                val docId = cursor.getString(idIndex)
                val mime = cursor.getString(mimeIndex)
                if (mime != null && mime.startsWith("video/")) {
                    videoList.add(DocumentsContract.buildDocumentUriUsingTree(treeUri, docId))
                }
            }
        }
        return videoList
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "Video"
        context.contentResolver.query(
            uri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                name = cursor.getString(idx) ?: name
            }
        }
        return name
    }
}
