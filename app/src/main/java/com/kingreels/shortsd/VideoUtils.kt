package com.kingreels.shortsd

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object VideoUtils {

    fun listVideosInFolder(context: Context, treeUri: Uri): List<Uri> {
        val docFile = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        return docFile.listFiles()
            .filter { it.isFile && it.type?.startsWith("video/") == true }
            .sortedBy { it.name }
            .mapNotNull { it.uri }
    }

    fun getFolderName(context: Context, treeUri: Uri): String {
        return DocumentFile.fromTreeUri(context, treeUri)?.name ?: "Folder"
    }

    fun getFileName(context: Context, fileUri: Uri): String {
        return DocumentFile.fromSingleUri(context, fileUri)?.name
            ?: fileUri.lastPathSegment
            ?: "Video"
    }
}
