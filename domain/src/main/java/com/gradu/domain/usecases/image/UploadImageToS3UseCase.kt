package com.gradu.domain.usecases.image

import android.net.Uri
import android.util.Log
import com.mongmong.namo.domain.repositories.ImageRepository
import javax.inject.Inject

class UploadImageToS3UseCase @Inject constructor(private val repository: ImageRepository) {

    suspend fun execute(prefix:String, images: List<Uri>): List<String> {
        val uploadResults = mutableListOf<String>()
        for (image in images) {
            val preSignedUrl = repository.getPreSignedUrl(prefix, image)

            if (preSignedUrl != null) {
                val success = repository.uploadImageToS3(preSignedUrl, image)
                if (success == 200) {
                    val uri = Uri.parse(preSignedUrl)
                    uploadResults.add(uri.scheme + "://" + uri.host + uri.path)
                }
            }
        }
        Log.d("UploadImageToS3UseCase", "$uploadResults")
        return uploadResults
    }
}
