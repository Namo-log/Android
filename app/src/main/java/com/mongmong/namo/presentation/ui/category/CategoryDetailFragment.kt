package com.mongmong.namo.presentation.ui.category

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.databinding.FragmentCategoryDetailBinding
import com.mongmong.namo.presentation.ui.category.CategorySettingFragment.Companion.CATEGORY_DATA
import com.mongmong.namo.presentation.ui.category.adapter.CategoryPaletteRVAdapter
import com.mongmong.namo.domain.model.Category
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.config.PaletteType
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.config.RoomState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryDetailFragment(private val isEditMode: Boolean)
    : BaseFragment<FragmentCategoryDetailBinding>(R.layout.fragment_category_detail) {

    private lateinit var paletteAdapter: CategoryPaletteRVAdapter

    private val viewModel : CategoryViewModel by viewModels()

    private lateinit var categoryList : List<CardView>
    private lateinit var checkList : List<ImageView>

    override fun setup() {
        binding.viewModel = this@CategoryDetailFragment.viewModel

        initBasicColor()

        setInit()
        switchToggle()
        onClickListener()
        clickCategoryItem()

        initObservers()

        if (viewModel.color.value == null) {
            initPaletteColorRv(CategoryColor.SCHEDULE)
        } else {
            initPaletteColorRv(viewModel.color.value!!)
        }
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
                if (!this@CategoryDetailFragment.viewModel.isValidInput()) {
                    Toast.makeText(requireContext(), "카테고리를 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // 수정 모드 -> 카테고리 update
                if (isEditMode) {
                    updateData()
                }
                // 생성 모드 -> 카테고리 insert
                else {
                    insertData()
                }
            }
        }
    }

    private fun setInit() {
        // 편집 모드
        if (isEditMode) {
            // 이전 화면에서 저장한 spf 받아오기
            loadPref()
            return
        }
        viewModel.setCategory(Category(isShare = true))
    }

    private fun initObservers() {
        viewModel.isComplete.observe(requireActivity()) { isComplete ->
            // 추가 작업이 완료된 후 뒤로가기
            if (isComplete) {
                viewModel.completeState.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        RoomState.ADDED -> Toast.makeText(requireContext(), "카테고리가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                        RoomState.EDITED -> Toast.makeText(requireContext(), "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        else -> {}
                    }
                }
                moveToSettingFrag(isEditMode)
            }
        }
    }

    /** 카테고리 추가 */
    private fun insertData() {
        // 새 카테고리 등록
        viewModel.addCategory()
    }

    private fun updateData() {
        // 카테고리 편집
        viewModel.editCategory()
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

    private fun initPaletteColorRv(initCategory: CategoryColor) {
        // 기본 팔레트
        val paletteDatas = CategoryColor.findPaletteByPaletteType(PaletteType.BASIC_PALETTE)

        for (i: Int in paletteDatas.indices) {
            if (paletteDatas[i] == viewModel.color.value) {
                viewModel.updateSelectedPalettePosition(i)
            }
        }

        if (viewModel.selectedPalettePosition.value == null) viewModel.updateSelectedPalettePosition(0)

        // 어댑터 연결
        paletteAdapter = CategoryPaletteRVAdapter(requireContext(), paletteDatas, initCategory, viewModel.selectedPalettePosition.value!!)
        binding.categoryPaletteRv.apply {
            adapter = paletteAdapter
            layoutManager = GridLayoutManager(context, 5)
        }
        // 아이템 클릭
        paletteAdapter.setColorClickListener(object: CategoryPaletteRVAdapter.MyItemClickListener {
            override fun onItemClick(position: Int, selectedColor: CategoryColor) {
                // 팔레트의 색상을 선택했다면 기본 색상의 체크 상태는 초기화
                for (j: Int in categoryList.indices) {
                    checkList[j].visibility = View.GONE
                }
                // 색상값 세팅
                viewModel.updateCategoryColor(selectedColor)
                // notifyItemChanged()에서 인자로 넘겨주기 위함. 기본 색상을 클릭했다면 이전에 선택된 팔레트 색상의 체크 표시는 해제
                viewModel.updateSelectedPalettePosition(position)
            }
        })
    }

    private fun clickCategoryItem() { // 기본 색상 선택
        for (i: Int in categoryList.indices) {
            categoryList[i].setOnClickListener {
                // 다른 모든 기본 색상 선택 해제
                for (j: Int in categoryList.indices) {
                    checkList[j].visibility = View.GONE
                }
                // 팔레트 내의 색상도 모두 선택 해제
                initPaletteColorRv(CategoryColor.SCHEDULE)
                if (viewModel.selectedPalettePosition.value != null) {
                    paletteAdapter.notifyItemChanged(viewModel.selectedPalettePosition.value!!)
                }
                // 선택한 카테고리 표시
                checkList[i].visibility = View.VISIBLE
                viewModel.updateCategoryColor(CategoryColor.findPaletteByPaletteType(PaletteType.DEFAULT_4)[i])
                // 이제 팔레트가 아니라 기본 색상에서 설정한 색임
                viewModel.updateSelectedPalettePosition(null)
            }
        }
    }

    private fun switchToggle() {
        val isShare = viewModel.category.value!!.isShare
        binding.categoryToggleIv.apply {
            // 첫 진입 시 토글 이미지 세팅
            isChecked = isShare
            // 토글 클릭 시 이미지 세팅
            setOnClickListener {
                (it as SwitchCompat).isChecked = !isShare
                viewModel.updateIsShare(!isShare)
            }
        }
    }

    private fun loadPref() {
        val spf = requireActivity().getSharedPreferences(CategorySettingFragment.CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)

        if (spf.contains(CATEGORY_DATA)) {
            val gson = Gson()
            val json = spf.getString(CATEGORY_DATA, "")
            try {
                // 데이터에 타입을 부여하기 위한 typeToken
                val typeToken = object : TypeToken<Category>() {}.type
                // 데이터 받기
                val data: Category = gson.fromJson(json, typeToken)
                viewModel.setCategory(data)

                // 데이터 값 넣어주기
                if (CategoryColor.findPaletteByPaletteType(PaletteType.DEFAULT_4).contains(viewModel.color.value)) {
                    // 기본 카테고리 체크 표시
                    checkList[viewModel.category.value!!.colorId - 1].visibility = View.VISIBLE
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
            requireActivity().supportFragmentManager
                .popBackStack() // 뒤로가기
        }
    }
}