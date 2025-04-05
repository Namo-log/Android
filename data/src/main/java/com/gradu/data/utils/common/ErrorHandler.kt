package com.gradu.data.utils.common

import com.mongmong.namo.domain.model.BaseResponse
import org.json.JSONObject
import retrofit2.HttpException

object ErrorHandler {
    // Throwable의 확장 함수로 에러 메시지 처리
    fun Throwable.handleError(): BaseResponse {
        return when (this) {
            is HttpException -> {
                val errorBody = this.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody).getString("message")
                } catch (e: Exception) {
                    "Unknown HTTP error"
                }
                BaseResponse(
                    code = code(),
                    message = errorMessage,
                    isSuccess = false
                )
            }
            else -> {
                BaseResponse(
                    code = -1,
                    message = message ?: "Unknown error",
                    isSuccess = false
                )
            }
        }
    }

}