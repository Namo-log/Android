package com.mongmong.namo.presentation.ui.home.calendar

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.mongmong.namo.databinding.DialogSetMonthBinding
import org.joda.time.DateTime

class SetMonthDialog(
    context : Context,
    private val millis : Long,
    private val okCallback : (DateTime) -> Unit
) : Dialog(context) {

    private val MAX_YEAR = 2099
    private val MIN_YEAR = 2000

    private lateinit var binding : DialogSetMonthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSetMonthBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        //뒤로가기 버튼, 빈 화면 터치를 통해 dialog 사라짐
        setCancelable(true)

        //background 투명하게
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.yearPicker.minValue = MIN_YEAR
        binding.yearPicker.maxValue = MAX_YEAR
        binding.monthPicker.minValue = 1
        binding.monthPicker.maxValue = 12

        val date = DateTime(millis)
        binding.yearPicker.value = date.year
        binding.monthPicker.value = date.monthOfYear

        clickListener()
    }

    private fun clickListener() {
        binding.acceptBtn.setOnClickListener {
            var date = DateTime(binding.yearPicker.value, binding.monthPicker.value, 1, 0, 0)
            okCallback(date)

            dismiss()
        }

        binding.cancelBtn.setOnClickListener {
            okCallback(DateTime(millis))

            dismiss()
        }
    }
}