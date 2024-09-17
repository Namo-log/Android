package com.mongmong.namo.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object RequestConverter {
    // String 리스트(이미지)를 MultipartBody.Part 리스트로 변환
    suspend fun imageToMultipart(imagePaths: List<String>?, context: Context): List<MultipartBody.Part>? = withContext(Dispatchers.IO) {
        imagePaths?.mapNotNull { path ->
            uriToMultipart(Uri.parse(path), context, true)
        }
    }

    // Uri를 MultipartBody.Part로 변환
    suspend fun uriToMultipart(uri: Uri, context: Context, isList: Boolean): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(uri, context)
            file?.let {
                val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                if(isList) MultipartBody.Part.createFormData("createImages", it.name, requestFile)
                else MultipartBody.Part.createFormData("img", it.name, requestFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 새로운 Uri를 RequestBody로 변환하는 함수
    suspend fun uriToRequestBody(uri: Uri, context: Context, mediaType: String = "image/jpeg"): RequestBody? = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(uri, context)
            file?.asRequestBody(mediaType.toMediaTypeOrNull())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun uriToFile(uri: Uri, context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadWebImageToBitmap(context, uri.toString())
            val bitmapHash = bitmap?.hashCode() ?: return@withContext null
            val fileName = "image_$bitmapHash.jpg"
            val imgFile = File(context.cacheDir, fileName)

            if (!imgFile.exists()) {
                FileOutputStream(imgFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                Log.d("cache", "Image saved: ${imgFile.path}")
                imgFile
            } else {
                Log.d("cache", "Image already exists: ${imgFile.path}")
                imgFile
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadWebImageToBitmap(context: Context, imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(RequestOptions()
                    .override(Target.SIZE_ORIGINAL) // 원본 크기로 로딩
                    .fitCenter() // 이미지를 중앙에 맞춤
                    .disallowHardwareConfig() // 하드웨어 가속을 사용하지 않음
                )
                .submit()
                .get() // 동기 로딩을 위해 get() 사용
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    fun String.convertTextRequest() = toRequestBody("text/plain".toMediaTypeOrNull())
}