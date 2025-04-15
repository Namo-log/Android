package com.gradu.domain.repositories

interface ImageRepository {

    suspend fun getPreSignedUrl(prefix: String, image: Uri): String?

    suspend fun uploadImageToS3(preSignedUrl: String, image: Uri): Int?
}