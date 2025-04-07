package com.gradu.domain.repositories

import com.sun.jndi.toolkit.url.Uri

interface ImageRepository {

    suspend fun getPreSignedUrl(prefix: String, image: Uri): String?

    suspend fun uploadImageToS3(preSignedUrl: String, image: Uri): Int?
}