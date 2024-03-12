package com.mongmong.namo.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageConverter {
    suspend fun imageToFile(images: List<String>?, context: Context): List<File>? = withContext(Dispatchers.IO) {
        images?.mapNotNull { path ->
            uriToFile(Uri.parse(path), context)
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
}