package com.mongmong.namo.presentation.ui.diary

import CalendarDay
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mongmong.namo.R
import com.mongmong.namo.databinding.BottomSheetDiaryCalendarBinding

class DiaryCalendarBottomSheet(
    private val calendarDay: CalendarDay
) : BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetDiaryCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetDiaryCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
}

