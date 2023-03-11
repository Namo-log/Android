package com.example.namo.ui.bottom.group

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import com.example.namo.databinding.DialogGroupCodeBinding

class GroupCodeDialog(
    context: Context,
    private val okCallback: (String) -> Unit,
) : Dialog(context) { // 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받는다.

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
        groupCodeSaveTv.setOnClickListener {
            if (groupCodeEt.text.isNullOrBlank()) {
                Toast.makeText(context, "그룹 코드를 입력하세요!", Toast.LENGTH_SHORT).show()
            } else {
                okCallback(groupCodeEt.text.toString())
                dismiss()
            }
        }
    }
}