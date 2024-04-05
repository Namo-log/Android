package com.mongmong.namo.presentation.ui.bottom.home.category

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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.databinding.FragmentCategoryDetailBinding
import com.mongmong.namo.presentation.ui.bottom.home.category.CategorySettingFragment.Companion.CATEGORY_KEY_DATA
import com.mongmong.namo.presentation.ui.bottom.home.category.adapter.CategoryPaletteRVAdapter
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.remote.category.CategoryDetailView
import com.mongmong.namo.data.remote.category.CategoryService
import com.mongmong.namo.domain.model.PostCategoryResponse
import com.mongmong.namo.presentation.utils.NetworkManager
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.mongmong.namo.data.local.entity.home.CategoryForUpload
import com.mongmong.namo.presentation.config.PaletteType
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryDetailFragment(private val isEditMode: Boolean) : Fragment(), CategoryDetailView {
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var paletteAdapter: CategoryPaletteRVAdapter

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    private val failList = ArrayList<Category>()

    private var clickable = true // 중복 생성을 방지하기 위함

    private val viewModel : CategoryViewModel by viewModels()

    // 카테고리에 들어갈 데이터
    var categoryId : Long = -1
    var name: String = ""
    var color: CategoryColor? = null
    var share: Boolean = true

    // 서버에 보낼 때 필요한 값
    var serverId: Long = 0
    var paletteId: Int = 0

    var selectedPalettePosition: Int? = null // 팔레트 -> 기본 색상 선택 시 사용될 변수

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
        switchToggle()
        onClickListener()
        clickCategoryItem()

        initObservers()

        if (color == null) {
            initPaletteColorRv(CategoryColor.SCHEDULE)
        } else {
            initPaletteColorRv(color!!)
        }

        return binding.root
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
                if (categoryDetailTitleEt.text.toString().isEmpty() || color == null) {
                    Toast.makeText(requireContext(), "카테고리를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    if (clickable) {
                        // 수정 모드 -> 카테고리 update
                        if (isEditMode) {
                            lifecycleScope.launch {
                                updateData()
                            }
                        }
                        // 생성 모드 -> 카테고리 insert
                        else {
                            lifecycleScope.launch {
                                insertData()
                            }
                        }
                    }
                    clickable = false
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

    private fun uploadToServer(state : String) {
        if (!NetworkManager.checkNetworkState(requireContext())) {
            // 인터넷 연결 안 됨
            // 룸디비에 isUpload, serverId, state 업데이트하기
            val thread = Thread {
                category = db.categoryDao.getCategoryWithId(categoryId)
                viewModel.updateCategoryAfterUpload(categoryId, UploadState.IS_NOT_UPLOAD.state, category.serverId, state)
                failList.clear()
                failList.addAll(db.categoryDao.getNotUploadedCategory() as ArrayList<Category>)
            }
            thread.start()
            try {
                thread.join()
            } catch ( e: InterruptedException) {
                e.printStackTrace()
            }

            Log.d("CategoryDetailFrag", "WIFI ERROR : $failList")

            // 화면 이동
            moveToSettingFrag(isEditMode)

            return
        }

        when(state) {
            RoomState.ADDED.state -> {
                // 카테고리 생성
                CategoryService(this@CategoryDetailFragment).tryPostCategory(CategoryForUpload(name, paletteId, share), categoryId)
            }
            RoomState.EDITED.state -> {
                // 카테고리 수정
                CategoryService(this@CategoryDetailFragment).tryPatchCategory(serverId, CategoryForUpload(name, paletteId, share), categoryId)
            }
            else -> {
                Log.d("CategoryDetailFrag", "서버 업로드 중 state 오류")
            }
        }
    }

    private fun initObservers() {
        viewModel.category.observe(requireActivity()) { category ->
            binding.categoryDetailTitleEt.setText(category.name)
        }
        viewModel.isPostComplete.observe(requireActivity()) { isComplete ->
            // 추가 작업이 완료된 후 뒤로가기
            if (isComplete) {
                moveToSettingFrag(isEditMode)
            }
        }
    }

    /** 카테고리 추가 */
    private fun insertData() {
        // RoomDB
        name = binding.categoryDetailTitleEt.text.toString()
        category = Category(0, name, color!!.paletteId, share)
        category.state = RoomState.ADDED.state

        // 새 카테고리 등록
        viewModel.addCategory(category)
    }

    private fun updateData() {
        // RoomDB
        name = binding.categoryDetailTitleEt.text.toString()
        category = Category(categoryId, name, color!!.paletteId, share)
        category.state = RoomState.EDITED.state
        category.serverId = serverId

        // 카테고리 편집
        viewModel.editCategory(category)

        Toast.makeText(requireContext(), "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()
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
            if (paletteDatas[i] == color) {
                selectedPalettePosition = i
                paletteId = color!!.paletteId
            }
        }

        if (selectedPalettePosition == null) selectedPalettePosition = 0

//        Log.d("CategoryDetailFrag", "selectedPalettePosition: $selectedPalettePosition")

        // 어댑터 연결
        paletteAdapter = CategoryPaletteRVAdapter(requireContext(), paletteDatas,
            initCategory, selectedPalettePosition!!)
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
                this@CategoryDetailFragment.color = selectedColor
                paletteId = selectedColor.paletteId
                // notifyItemChanged()에서 인자로 넘겨주기 위함. 기본 색상을 클릭했다면 이전에 선택된 팔레트 색상의 체크 표시는 해제
                selectedPalettePosition = position
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
                if (selectedPalettePosition != null) {
                    paletteAdapter.notifyItemChanged(selectedPalettePosition!!)
                }
                // 선택한 카테고리 표시
                checkList[i].visibility = View.VISIBLE
                color = CategoryColor.findPaletteByPaletteType(PaletteType.DEFAULT_4)[i]
                paletteId = color!!.paletteId
                // 이제 팔레트가 아니라 기본 색상에서 설정한 색임
                selectedPalettePosition = null
            }
        }
    }

    private fun switchToggle() {
        val toggle = binding.categoryToggleIv
        // 첫 진입 시 토글 이미지 세팅
        toggle.isChecked = share
        // 토글 클릭 시 이미지 세팅
        toggle.setOnClickListener {
            toggle.isChecked = !share
            share = !share
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
                    categoryId = data.categoryId
                    serverId = data.serverId
                    // 카테고리 이름
                    categoryDetailTitleEt.setText(data.name)
                    // 카테고리 색
                    color = CategoryColor.findCategoryColorByPaletteId(data.paletteId)
                    if (CategoryColor.findPaletteByPaletteType(PaletteType.DEFAULT_4).contains(color)) {
                        // 기본 카테고리 체크 표시
                        checkList[color!!.paletteId - 1].visibility = View.VISIBLE
                        paletteId = color!!.paletteId
                    }
                    // 카테고리 공유 여부
                    share = data.isShare
                }

                Log.e("CategoryDetailFrag", "roomId: ${categoryId}, serverId: ${serverId}")
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
            // 이전의 모든 프래그먼트를 백 스택에서 제거 (HomeFragment)
            requireActivity().supportFragmentManager
                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
    private fun updateCategoryAfterUpload(response: PostCategoryResponse?, state: String) {
        val result = response?.result

        when (state) {
            // 서버 통신 성공
            RoomState.DEFAULT.state -> {
                val thread = Thread {
                    viewModel.updateCategoryAfterUpload(categoryId, UploadState.IS_UPLOAD.state, result!!.categoryId, state)
                    db.categoryDao.updateCategory(category.copy(serverId = result.categoryId))
                }
                thread.start()
                try {
                    thread.join()
                } catch ( e: InterruptedException) {
                    e.printStackTrace()
                }
//                Log.e("CategoryDetailFrag", "serverId 업데이트 성공, 업데이트 serverId: ${category.serverId}, 실제: ${result!!.categoryId}")
            }
            // 서버 업로드 실패
            else -> {
                val thread = Thread {
                    viewModel.updateCategoryAfterUpload(categoryId, UploadState.IS_NOT_UPLOAD.state, serverId, state)
                    failList.clear()
                    failList.addAll(db.categoryDao.getNotUploadedCategory() as ArrayList<Category>)
                }
                thread.start()
                try {
                    thread.join()
                } catch ( e: InterruptedException) {
                    e.printStackTrace()
                }
                Log.d("CategoryDetailFrag", "Server Fail : $failList")
            }
        }

        // 화면 이동
        moveToSettingFrag(isEditMode)
    }


    // 카테고리 생성
    override fun onPostCategorySuccess(response: PostCategoryResponse, categoryId : Long) {
        Log.d("CategoryDetailFrag", "onPostCategorySuccess, categoryId = $categoryId")
        // 룸디비에 isUpload, serverId, state 업데이트하기
        updateCategoryAfterUpload(response, RoomState.DEFAULT.state)
    }

    override fun onPostCategoryFailure(message: String) {
        Log.d("CategoryDetailFrag", "onPostCategoryFailure")
        // 룸디비에 failList 업데이트하기
        updateCategoryAfterUpload(null, RoomState.ADDED.state)
    }

    // 카테고리 수정
    override fun onPatchCategorySuccess(response: PostCategoryResponse, categoryId: Long) {
        Log.d("CategoryDetailFrag", "onPatchCategorySuccess, categoryId = $categoryId")
        // 룸디비에 isUpload, serverId, state 업데이트하기
        updateCategoryAfterUpload(response, RoomState.DEFAULT.state)
    }

    override fun onPatchCategoryFailure(message: String) {
        Log.d("CategoryDetailFrag", "onPatchCategoryFailure")
        // 룸디비에 failList 업데이트하기
        updateCategoryAfterUpload(null, RoomState.EDITED.state)
    }
}