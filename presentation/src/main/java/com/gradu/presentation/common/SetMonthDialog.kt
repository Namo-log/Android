package com.gradu.presentation.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import com.gradu.presentation.databinding.DialogSetMonthBinding
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class SetMonthDialog(
    context: Context,
    private val millis: Long,
    private val okCallback: (LocalDateTime) -> Unit
) : Dialog(context) {

    private val MAX_YEAR = 2099
    private val MIN_YEAR = 2000

    private lateinit var binding: DialogSetMonthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSetMonthBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        setCancelable(true)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        binding.yearPicker.minValue = MIN_YEAR
        binding.yearPicker.maxValue = MAX_YEAR
        binding.monthPicker.minValue = 1
        binding.monthPicker.maxValue = 12

        val date = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        binding.yearPicker.value = date.year
        binding.monthPicker.value = date.month.value

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.acceptBtn.setOnClickListener {
            val selectedDate = LocalDateTime.of(
                binding.yearPicker.value,
                binding.monthPicker.value,
                1, 0, 0, 0
            )
            okCallback(selectedDate)
            dismiss()
        }

        binding.cancelBtn.setOnClickListener {
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            okCallback(date)
            dismiss()
        }
    }
}
