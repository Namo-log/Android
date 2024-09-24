package com.mongmong.namo.presentation.ui.diary


import android.view.MotionEvent
import androidx.activity.viewModels
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityDiaryBinding
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiaryActivity : BaseActivity<ActivityDiaryBinding>(R.layout.activity_diary) {
    private var isCalendarView = false

    override fun setup() {
        initClickListener()

        showDiaryCollectFragment()
    }

    private fun initClickListener() {
        binding.diaryBackBtn.setOnClickListener { finish() }

        binding.diaryCalendarBtn.setOnClickListener {
            if (isCalendarView) {
                showDiaryCollectFragment()
            } else {
                showDiaryCalendarFragment()
            }
        }
    }

    private fun showDiaryCollectFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DiaryCollectionFragment())
            .commit()

        binding.diaryCalendarBtn.setImageResource(R.drawable.ic_calendar)
        isCalendarView = false
    }

    private fun showDiaryCalendarFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DiaryCalendarFragment())
            .commit()

        binding.diaryCalendarBtn.setImageResource(R.drawable.ic_collect)
        isCalendarView = true
    }

    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }
}

