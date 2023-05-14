package com.example.namo.ui.bottom.home.schedule.category

import android.content.Context
import android.content.Intent
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
import com.example.namo.ui.bottom.home.schedule.category.CategorySettingFragment.Companion.CATEGORY_KEY_DATA
import com.example.namo.ui.bottom.home.schedule.data.Category
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

class CategoryDetailFragment(val isEditMode: Boolean) : Fragment() {
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    var categoryIdx = -1
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

        checkEditingMode(isEditMode)
        onClickListener()
        clickCategoryItem()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setToggle(share)
    }

    private fun onClickListener() {
        with(binding) {
            // 뒤로가기
            categoryDetailBackIv.setOnClickListener {
                if (isEditMode) {
                    // 편집 모드라면 CategoryEditActivity 위에 Fragment 씌어짐
                    startActivity(Intent(requireActivity(), CategoryActivity::class.java))
                    activity?.finish()
                } else {
                    (context as CategoryActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.category_frm, CategorySettingFragment())
                        .commitAllowingStateLoss()
                }
            }

            // 저장하기
            categoryDetailSaveTv.setOnClickListener {
                if (categoryDetailTitleEt.text.toString().isEmpty() || color == 0) {
                    Toast.makeText(requireContext(), "카테고리를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {

                    /* 수정 모드 -> 카테고리 update */
                    if (isEditMode) {
                        //
                    }

                    /* 생성 모드 -> 카테고리 insert */
                    else {
                        // 카테고리 추가
                        insertData()
                        // 화면 이동
                        (context as CategoryActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.category_frm, CategorySettingFragment())
                            .commitAllowingStateLoss()
                    }
                }
            }

            // 토글 버튼 활성화/비활성화
            categoryToggleIv.setOnClickListener {
                share = switchToggle(share)
                Log.d("CategoryDetailFrag", "share: $share")
            }
        }
    }

    private fun checkEditingMode(isEditMode: Boolean) {
        // 편집 모드
        if (isEditMode) {
            // 이전 화면에서 저장한 spf 받아오기
            loadPref()
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

    private fun setToggle(isShare: Boolean) {
        val toggle = binding.categoryToggleIv
        if (isShare) {
            toggle.setImageResource(R.drawable.ic_toggle_on)
        }
        else {
            toggle.setImageResource(R.drawable.ic_toggle_off)
        }
    }
    private fun switchToggle(isShare: Boolean): Boolean {
        val toggle = binding.categoryToggleIv
        if (isShare) {
            toggle.setImageResource(R.drawable.ic_toggle_off)
        }
        else {
            toggle.setImageResource(R.drawable.ic_toggle_on)
        }
        return !isShare
    }

    private fun loadPref() {
        val spf = requireActivity().getSharedPreferences(CategorySettingFragment.CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)

        if (spf.contains(CATEGORY_KEY_DATA)) {
            val gson = Gson()
            val json = spf.getString(CATEGORY_KEY_DATA, "")
            try {
                // 데이터에 타입을 부여하기 위한 typeToken
                val typeToken = object : TypeToken<Category>() {}.type
                // 데이터 받기
                val data: Category = gson.fromJson(json, typeToken)

                // 데이터 값 넣어주기
                with(binding) {

                    val checkList = listOf(
                        scheduleColorSelectIv, schedulePlanColorSelectIv, scheduleParttimeColorSelectIv, scheduleGroupColorSelectIv
                    )
                    val colorList = listOf(
                        R.color.schedule, R.color.schedule_plan, R.color.schedule_parttime, R.color.schedule_group
                    )

                    //카테고리 ID로 넘겨받은 카테고리 세팅
                    categoryIdx = data.categoryIdx

                    // 카테고리 이름
                    categoryDetailTitleEt.setText(data.name)

                    // 카테고리 색
                    for (i: Int in colorList.indices) {
                        if (data.color == colorList[i]) {
                            checkList[i].visibility = View.VISIBLE
                            color = data.color
                        }
                    }

                    // 카테고리 공유 여부
                    share = data.share
                    setToggle(share)

                }
            } catch (e: JsonParseException) { // 파싱이 안 될 경우
                e.printStackTrace()
            }
            Log.d("debug", "Category Data loaded")
        }
    }
}