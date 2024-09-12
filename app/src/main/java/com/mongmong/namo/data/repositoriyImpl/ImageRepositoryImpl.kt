package com.mongmong.namo.data.repositoriyImpl

import android.content.Context
import android.net.Uri
import com.mongmong.namo.data.datasource.s3.ImageDataSource
import com.mongmong.namo.data.utils.RequestConverter
import com.mongmong.namo.domain.repositories.ImageRepository
import com.mongmong.namo.presentation.utils.FileUtils.getFileNameFromUri
import okhttp3.RequestBody
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val awsS3DataSource: ImageDataSource,
    private val context: Context
) :ImageRepository {

    override suspend fun getPreSignedUrl(
        prefix: String,
        image: Uri
    ): String? {
        return getFileNameFromUri(context, image)?.let {
            awsS3DataSource.getPreSignedUrl(prefix, it).result
        }
    }

    override suspend fun uploadImageToS3(
        preSignedUrl: String,
        image: Uri
    ): Int? {
        val requestBody = RequestConverter.uriToRequestBody(image, context)

        return requestBody?.let { awsS3DataSource.uploadImageToS3(preSignedUrl, it) }
    }

}