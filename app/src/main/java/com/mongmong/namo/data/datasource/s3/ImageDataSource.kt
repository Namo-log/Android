package com.mongmong.namo.data.datasource.s3

import com.mongmong.namo.data.dto.GetPreSignedUrlResponse
import com.mongmong.namo.data.remote.AwsS3ApiService
import com.mongmong.namo.data.remote.ImageApiService
import okhttp3.RequestBody
import javax.inject.Inject

class ImageDataSource @Inject constructor(
    private val awsS3Api: AwsS3ApiService,
    private val imageApi: ImageApiService
) {

    suspend fun getPreSignedUrl(
        prefix: String,
        fileName: String
    ): GetPreSignedUrlResponse {
        return imageApi.getPreSignedUrl(prefix, fileName)
    }

    suspend fun uploadImageToS3(
        preSignedUrl: String,
        image: RequestBody
    ): Int {
        return awsS3Api.uploadImageToS3(preSignedUrl, image).code()
    }
}