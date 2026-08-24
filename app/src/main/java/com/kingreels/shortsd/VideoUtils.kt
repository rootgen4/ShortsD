package com.kingreels.shortsd

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

object VideoUtils {

    fun listVideosInFolder(context: Context, treeUri: Uri): List<Uri> {
        val docFile = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        return docFile.listFiles()
            .filter { it.isFile && (it.type?.startsWith("video/") == true || it.name?.endsWith(".mp4", true) == true) }
            .sortedBy { it.name }
            .mapNotNull { it.uri }
    }

    fun getFolderName(context: Context, treeUri: Uri): String {
        return DocumentFile.fromTreeUri(context, treeUri)?.name ?: "Folder"
    }

    // Overload for String path if called anywhere
    fun getFolderName(path: String): String {
        return File(path).parentFile?.name ?: File(path).name
    }

    fun getFileName(context: Context, fileUri: Uri): String {
        return DocumentFile.fromSingleUri(context, fileUri)?.name
            ?: fileUri.lastPathSegment
            ?: "Video"
    }
}
