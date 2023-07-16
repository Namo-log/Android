package com.example.namo.ui.bottom.home.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.FragmentCategoryDetailBinding
import com.example.namo.ui.bottom.home.category.CategorySettingFragment.Companion.CATEGORY_KEY_DATA
import com.example.namo.ui.bottom.home.category.adapter.CategoryPaletteRVAdapter
import com.example.namo.data.entity.home.Category
import com.example.namo.data.remote.category.CategoryBody
import com.example.namo.data.remote.category.CategoryDetailView
import com.example.namo.data.remote.category.CategoryService
import com.example.namo.data.remote.category.PostCategoryResponse
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

class CategoryDetailFragment(private val isEditMode: Boolean) : Fragment(), CategoryDetailView {
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var paletteAdapter: CategoryPaletteRVAdapter

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    // 카테고리에 들어갈 데이터
    var categoryIdx = -1
    var name: String = ""
    var color: Int = 0
    var share: Boolean = true

    var selectedPalettePosition: Int? = null // 팔레트 -> 기본 색상 선택 시 사용될 변수

    private val colorList = listOf( // 기본 색상 리스트
        R.color.schedule, R.color.schedule_plan, R.color.schedule_parttime, R.color.schedule_group
    )

    private lateinit var categoryList : List<CardView>
    private lateinit var checkList : List<ImageView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)

        db = NamoDatabase.getInstance(requireContext())

        initBasicColor()

        checkEditingMode(isEditMode)
        switchToggle(share)
        onClickListener()
        clickCategoryItem()
        initPaletteColorRv(color)

        return binding.root
    }

    override fun onStart() {
        super.onStart()


    }

    private fun onClickListener() {
        with(binding) {
            // 뒤로가기
            categoryDetailBackIv.setOnClickListener {
                // 편집 모드라면 CategoryEditActivity 위에 Fragment 씌어짐
                moveToSettingFrag(isEditMode)
            }

            // 저장하기
            categoryDetailSaveTv.setOnClickListener {
                if (categoryDetailTitleEt.text.toString().isEmpty() || color == 0) {
                    Toast.makeText(requireContext(), "카테고리를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    // 수정 모드 -> 카테고리 update
                    if (isEditMode) {
                        updateData()
                    }
                    // 생성 모드 -> 카테고리 insert
                    else {
                        insertData()
                    }
                    // 화면 이동
                    moveToSettingFrag(isEditMode)
                }
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
        // RoomDB
        Thread{
            name = binding.categoryDetailTitleEt.text.toString()
            category = Category(0, name, color, share)
            db.categoryDao.insertCategory(category)
        }.start()
        // 서버 통신
        CategoryService(this@CategoryDetailFragment).tryPostCategory(CategoryBody(name, color, share))
    }

    private fun updateData() {
        // RoomDB
        val thread = Thread{
            name = binding.categoryDetailTitleEt.text.toString()
            category = Category(categoryIdx, name, color, share)
            db.categoryDao.updateCategory(category)
            Log.d("CategoryDetailFragment", "updateCategory: ${db.categoryDao.getCategoryContent(categoryIdx)}")
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
        // 서버 통신
        CategoryService(this@CategoryDetailFragment).tryPatchCategory(categoryIdx, CategoryBody(name, color, share))

        updateEventWithCategory()
    }

    private fun updateEventWithCategory() {
        val thread = Thread {
            val eventList = db.eventDao.getEventWithCategoryIdx(categoryIdx)
            Log.d("UPDATE_BEFORE", eventList.toString())
            for (i in eventList) {
                i.categoryName = name
                i.categoryColor = color

                db.eventDao.updateEvent(i)
            }

            Log.d("UPDATE_AFTER", eventList.toString())
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun initBasicColor() {
        // 기본 색상 관련 리스트 설정
        with (binding) {
            categoryList = listOf(
                scheduleColorCv, schedulePlanColorCv, scheduleParttimeColorCv, scheduleGroupColorCv
            )
            checkList = listOf(
                scheduleColorSelectIv, schedulePlanColorSelectIv, scheduleParttimeColorSelectIv, scheduleGroupColorSelectIv
            )
        }
    }

    private fun initPaletteColorRv(initColor: Int) {

        // 더미데이터 냅다 집어 넣기
        val paletteDatas = arrayListOf(
            R.color.palette1, R.color.palette2, R.color.palette3, R.color.palette4, R.color.palette5,
            R.color.palette6, R.color.palette7, R.color.palette8, R.color.palette9, R.color.palette10
        )

        for (i: Int in paletteDatas.indices) {
            if (paletteDatas[i] == color) {
                selectedPalettePosition = i
            }
        }

        if (selectedPalettePosition == null) selectedPalettePosition = 0

//        Log.d("CategoryDetailFrag", "selectedPalettePosition: $selectedPalettePosition")

        // 어댑터 연결
        paletteAdapter = CategoryPaletteRVAdapter(requireContext(), paletteDatas, initColor, selectedPalettePosition!!)
        binding.categoryPaletteRv.apply {
            adapter = paletteAdapter
            layoutManager = GridLayoutManager(context, 5)
        }
        // 아이템 클릭
        paletteAdapter.setColorClickListener(object: CategoryPaletteRVAdapter.MyItemClickListener {
            override fun onItemClick(position: Int, selectedColor: Int) {
                // 팔레트의 색상을 선택했다면 기본 색상의 체크 상태는 초기화
                for (j: Int in categoryList.indices) {
                    checkList[j].visibility = View.GONE
                }
                // 색상값 세팅
                this@CategoryDetailFragment.color = selectedColor
                // notifyItemChanged()에서 인자로 넘겨주기 위함. 기본 색상을 클릭했다면 이전에 선택된 팔레트 색상의 체크 표시는 해제
                selectedPalettePosition = position
            }
        })
    }

    private fun clickCategoryItem() {
        for (i: Int in categoryList.indices) {
            categoryList[i].setOnClickListener {
                // 다른 모든 기본 색상 선택 해제
                for (j: Int in categoryList.indices) {
                    checkList[j].visibility = View.GONE
                }
                // 팔레트 내의 색상도 모두 선택 해제
                initPaletteColorRv(-1)
                if (selectedPalettePosition != null) {
                    paletteAdapter.notifyItemChanged(selectedPalettePosition!!)
                }
                // 선택한 카테고리 표시
                checkList[i].visibility = View.VISIBLE
                color = colorList[i]
                // 이제 팔레트가 아니라 기본 색상에서 설정한 색임
                selectedPalettePosition = null
            }
        }
    }

    private fun switchToggle(isShare: Boolean) {
        val toggle = binding.categoryToggleIv
        val toggleImg = listOf(
            R.drawable.ic_toggle_off, R.drawable.ic_toggle_on
        )
        // 첫 진입 시 토글 이미지 세팅
        if (isShare) toggle.setImageResource(toggleImg[1])
        else toggle.setImageResource(toggleImg[0])
        // 토글 클릭 시 이미지 세팅
        toggle.setOnClickListener {
            if (isShare) toggle.setImageResource(toggleImg[0])
            else toggle.setImageResource(toggleImg[1])

            share = !isShare
        }
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

                    //카테고리 ID로 넘겨받은 카테고리 세팅
                    categoryIdx = data.categoryIdx

                    // 카테고리 이름
                    categoryDetailTitleEt.setText(data.name)

                    // 카테고리 색
                    color = data.color
                    for (i: Int in colorList.indices) {
                        if (data.color == colorList[i]) {
                            // 기본 카테고리 체크 표시
                            checkList[i].visibility = View.VISIBLE
                        }
                    }

                    // 카테고리 공유 여부
                    share = data.share
                }
            } catch (e: JsonParseException) { // 파싱이 안 될 경우
                e.printStackTrace()
            }
            Log.d("debug", "Category Data loaded")
        }
    }

    private fun moveToSettingFrag(isEditMode: Boolean) {
        if (isEditMode) { // 편집 모드에서의 화면 이동
            Intent(requireActivity(), CategoryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }.also {
                startActivity(it)
            }
            activity?.finish()
        } else { // 생성 모드에서의 화면 이동
            (context as CategoryActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.category_frm, CategorySettingFragment())
                .commitAllowingStateLoss()
        }
    }

    // RoomDB 카테고리와 서버 카테고리 동기화
    private fun updateCategoryId(id: Int) {
        Thread{
            category = db.categoryDao.getCategoryContent(categoryIdx)
            // 삭제 대신 비활성화 처리
            db.categoryDao.updateCategory(category.copy(categoryIdx = id))
        }.start()
    }


    // 카테고리 생성
    override fun onPostCategorySuccess(response: PostCategoryResponse) {
        Log.d("CategoryDetailFrag", "onPostCategorySuccess, categoryIdx = $categoryIdx")
        //TODO: 서버 업로드 변수 update
        updateCategoryId(response.result.categoryIdx)
    }

    override fun onPostCategoryFailure(message: String) {
        Log.d("CategoryDetailFrag", "onPostCategoryFailure")
    }

    // 카테고리 수정
    override fun onPatchCategorySuccess(response: PostCategoryResponse) {
        Log.d("CategoryDetailFrag", "onPatchCategorySuccess, categoryIdx = $categoryIdx")
        //TODO: 서버 업로드 변수 update
    }

    override fun onPatchCategoryFailure(message: String) {
        Log.d("CategoryDetailFrag", "onPatchCategoryFailure")
    }
}