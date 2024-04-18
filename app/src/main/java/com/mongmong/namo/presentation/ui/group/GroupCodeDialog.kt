package com.mongmong.namo.presentation.ui.group

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mongmong.namo.databinding.DialogGroupCodeBinding
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class GroupCodeDialog() : DialogFragment() { // 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받는다.

    private lateinit var binding: DialogGroupCodeBinding

    private val viewModel : GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogGroupCodeBinding.inflate(layoutInflater)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initViews()
        initObserve()

        return binding.root
    }

    private fun initViews() = with(binding) {
        isCancelable = false

        groupCodeBackTv.setOnClickListener { dismiss() }
        // 저장 클릭
        groupCodeSaveTv.setOnClickListener {
            if (groupCodeEt.text.isNullOrBlank()) {
                Toast.makeText(context, "그룹 코드를 입력하세요!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.joinGroup(groupCodeEt.text.toString())
            }
        }
    }

    private fun initObserve() {
        viewModel.joinGroupResult.observe(viewLifecycleOwner) {
            if(it.result != 0L) {
                Toast.makeText(context, "모임 참여에 성공했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, extractMessageFromResponse(it.message), Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
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