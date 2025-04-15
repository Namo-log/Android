package com.gradu.data.repositoriyImpl

import android.content.Context
import android.net.Uri
import com.gradu.data.datasource.s3.ImageDataSource
import com.gradu.data.utils.common.RequestConverter
import com.gradu.data.utils.common.RequestConverter.getFileNameFromUri
import com.gradu.domain.repositories.ImageRepository
import com.mongmong.namo.data.utils.common.RequestConverter
import com.mongmong.namo.data.utils.common.RequestConverter.getFileNameFromUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val awsS3DataSource: ImageDataSource,
    @ApplicationContext private val context: Context
) : ImageRepository {

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