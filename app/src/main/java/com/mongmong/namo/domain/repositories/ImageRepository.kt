package com.mongmong.namo.domain.repositories

import android.net.Uri


interface ImageRepository {

    suspend fun getPreSignedUrl(prefix: String, image: Uri): String?

    suspend fun uploadImageToS3(preSignedUrl: String, image: Uri): Int?
}