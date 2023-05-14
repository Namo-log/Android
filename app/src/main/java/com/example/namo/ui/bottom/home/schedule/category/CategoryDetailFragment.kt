package com.example.namo.ui.bottom.home.schedule.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.FragmentCategoryDetailBinding
import com.example.namo.ui.bottom.home.schedule.data.Category

class CategoryDetailFragment(val isEditMode: Boolean) : Fragment() {
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    var name: String = ""
    var color: Int = 0
    var share: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)

        db = NamoDatabase.getInstance(requireContext())

        onClickListener()
        clickCategoryItem()

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
                if (categoryDetailTitleEt.text.toString().isEmpty() || color == 0) {
                    Toast.makeText(requireContext(), "카테고리를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    // 카테고리 추가
                    insertData()
                    // 화면 이동
                    (context as CategoryActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.category_frm, CategorySettingFragment())
                        .commitAllowingStateLoss()
                }
            }

            // 토글 버튼 활성화/비활성화
            categoryToggleIv.setOnClickListener {
                setToggle()
            }
        }
    }

    private fun insertData() {
        Thread{
            name = binding.categoryDetailTitleEt.text.toString()
            category = Category(0, name, color, share)
            db.categoryDao.insertCategory(category)
            Log.d("CategoryDetailFragment", "categoryDao: ${db.categoryDao.getCategoryList()}")
        }.start()
    }

    private fun clickCategoryItem() {
        with(binding) {
            val categoryList = listOf(
                scheduleColorCv, schedulePlanColorCv, scheduleParttimeColorCv, scheduleGroupColorCv
            )
            val checkList = listOf(
                scheduleColorSelectIv, schedulePlanColorSelectIv, scheduleParttimeColorSelectIv, scheduleGroupColorSelectIv
            )
            val colorList = listOf(
                R.color.schedule, R.color.schedule_plan, R.color.schedule_parttime, R.color.schedule_group
            )

            for (i: Int in categoryList.indices) {
                categoryList[i].setOnClickListener {
                    for (j: Int in categoryList.indices) { // 다른 것들 체크 상태 초기화
                        checkList[j].visibility = View.GONE
                    }
                    // 선택한 카테고리 표시
                    checkList[i].visibility = View.VISIBLE
                    color = colorList[i]
                }
            }
        }
    }

    private fun setToggle() {
        val toggle = binding.categoryToggleIv
        if (share) {
            toggle.setImageResource(R.drawable.ic_toggle_off)
            share = false
        }
        else {
            toggle.setImageResource(R.drawable.ic_toggle_on)
            share = true
        }
    }
}