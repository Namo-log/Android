package com.example.namo.ui.bottom.home.schedule.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.FragmentCategoryDetailBinding

class CategoryDetailFragment(val isEditMode: Boolean) : Fragment() {
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var db : NamoDatabase

    var share: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)

        db = NamoDatabase.getInstance(requireContext())

        onClickListener()
        clickCategoryItem()
        setToggle()

        return binding.root
    }

    override fun onStart() {
        super.onStart()


    }

    private fun onClickListener() {
        with(binding) {
            // 뒤로가기
            categoryDetailBackIv.setOnClickListener {
                (context as CategoryActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.category_frm, CategorySettingFragment())
                    .commitAllowingStateLoss()
            }

            // 저장하기
            categoryDetailSaveTv.setOnClickListener {
                // 저장 시 insert 동작 추가
                (context as CategoryActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.category_frm, CategorySettingFragment())
                    .commitAllowingStateLoss()
            }

            // 토글 버튼 활성화/비활성화
            categoryToggleIv.setOnClickListener {
                setToggle()
            }
        }
    }

    private fun clickCategoryItem() {
        with(binding) {
            val colorList = listOf(
                scheduleColorCv, schedulePlanColorCv, scheduleParttimeColorCv, scheduleGroupColorCv
            )
            val checkList = listOf(
                scheduleColorSelectIv, schedulePlanColorSelectIv, scheduleParttimeColorSelectIv, scheduleGroupColorSelectIv
            )

            for (i: Int in colorList.indices) {
                colorList[i].setOnClickListener {
                    for (j: Int in colorList.indices) { // 다른 것들 체크 상태 초기화
                        checkList[j].visibility = View.GONE
                    }
                    checkList[i].visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setToggle() {
        val toggle = binding.categoryToggleIv
        if (share == 1) {
            toggle.setImageResource(R.drawable.ic_toggle_off)
            share = 0
        }
        else {
            toggle.setImageResource(R.drawable.ic_toggle_on)
            share = 1
        }
    }
}