package com.mongmong.namo.presentation.ui.bottom.group

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mongmong.namo.data.remote.group.MoimService
import com.mongmong.namo.domain.model.ParticipateMoimResponse
import com.mongmong.namo.data.remote.group.ParticipateMoimView
import com.mongmong.namo.databinding.DialogGroupCodeBinding
import org.json.JSONObject

class GroupCodeDialog(
    context: Context,
    private val okCallback: (String) -> Unit,
) : Dialog(context), ParticipateMoimView { // 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받는다.

    private lateinit var binding: DialogGroupCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogGroupCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() = with(binding) {

        setCancelable(false)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        groupCodeBackTv.setOnClickListener {
            dismiss()
        }
        // 저장 클릭
        groupCodeSaveTv.setOnClickListener {
            if (groupCodeEt.text.isNullOrBlank()) {
                Toast.makeText(context, "그룹 코드를 입력하세요!", Toast.LENGTH_SHORT).show()
            } else {
                val moimService = MoimService()
                moimService.setParticipateMoimView(this@GroupCodeDialog)

                moimService.participateMoim(groupCodeEt.text.toString())
                okCallback(groupCodeEt.text.toString())
            }
        }
    }

    override fun onParticipateMoimSuccess(response: ParticipateMoimResponse) {
        Log.d("GroupCodeDig", "onParticipateMoimSuccess : ${response.result}")
        Toast.makeText(context, "모임 참여에 성공했습니다.", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onParticipateMoimFailure(message: String) {
        Log.d("GroupCodeDig", "onParticipateMoimFailure : $message")

        Toast.makeText(context, extractMessageFromResponse(message), Toast.LENGTH_SHORT).show()
        dismiss()
//        "message":"이미 가입한 모임입니다.","code":404
    }

    private fun extractMessageFromResponse(responseString: String): String {
        return try {
            val jsonObject = JSONObject(responseString)
            jsonObject.getString("message")
        } catch (e: Exception) {
            // "message" 필드가 없는 경우 전체 문자열 반환
            responseString
        }
    }

}