package com.mongmong.namo.presentation.ui.community.diary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mongmong.namo.R
import com.mongmong.namo.databinding.DialogActivityTagBinding

class ActivityTagDialog(private val position: Int) : DialogFragment() {

    private lateinit var binding: DialogActivityTagBinding
    private val viewModel: MoimDiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogActivityTagBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 초기 태그 설정
        setInitialTagSelection(viewModel.activities.value?.get(position)?.tag ?: "")

        binding.activityTagRadioInput.setOnClickListener {
            binding.activityTagRadioGroup.clearCheck()
            binding.activityTagRadioInput.isChecked = true
            binding.activityTagInputEt.isEnabled = true
        }

        binding.activityTagRadioGroup.setOnCheckedChangeListener { _, _ ->
            binding.activityTagRadioInput.isChecked = false
            binding.activityTagInputEt.isEnabled = false
        }

        binding.activityTagSaveTv.setOnClickListener {
            val selectedTag = when {
                binding.activityTagRadioInput.isChecked -> {
                    binding.activityTagInputEt.text.toString()
                }
                binding.activityTagRadioNone.isChecked -> ""
                else -> {
                    val radioButton = binding.root.findViewById<RadioButton>(binding.activityTagRadioGroup.checkedRadioButtonId)
                    radioButton?.text.toString()
                }
            }

            // ViewModel에 선택된 tag 업데이트
            viewModel.updateActivityTag(position, selectedTag)
            dismiss()
        }

        // 닫기 버튼 클릭 시
        binding.activityTagBackTv.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    // 태그를 초기 설정하는 함수
    private fun setInitialTagSelection(tag: String) {
        if (tag.isEmpty()) {
            binding.activityTagRadioNone.isChecked = true // 빈 태그일 경우 none을 선택
        } else {
            val tagOptions = listOf(
                binding.activityTagRadio1,
                binding.activityTagRadio2,
                binding.activityTagRadio3
            )

            val matchedRadioButton = tagOptions.find { it.text.toString() == tag }
            if (matchedRadioButton != null) {
                matchedRadioButton.isChecked = true
            } else {
                binding.activityTagRadioInput.isChecked = true
                binding.activityTagInputEt.setText(tag)
                binding.activityTagInputEt.isEnabled = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),  // 화면 너비의 80%로 설정
            ViewGroup.LayoutParams.WRAP_CONTENT  // 높이는 WRAP_CONTENT로 유지
        )
    }
}
