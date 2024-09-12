package com.mongmong.namo.presentation.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtils {

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            var fileName: String? = null
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
            return fileName
        } else {
            return uri.lastPathSegment?.substringAfterLast('/')
        }
    }
}
