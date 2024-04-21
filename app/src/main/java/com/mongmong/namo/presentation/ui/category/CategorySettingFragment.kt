package com.mongmong.namo.presentation.ui.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.databinding.FragmentCategorySettingBinding
import com.mongmong.namo.presentation.ui.category.adapter.SetCategoryRVAdapter
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.remote.category.CategorySettingView
import com.mongmong.namo.domain.model.GetCategoryResponse
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategorySettingFragment: Fragment(), CategorySettingView {

    lateinit var binding: FragmentCategorySettingBinding //플로팅 카테고리 설정 화면

    private lateinit var categoryRVAdapter: SetCategoryRVAdapter

    private var categoryList: ArrayList<Category> = arrayListOf() // arrayListOf<Category>()

    private var gson: Gson = Gson()
    private val viewModel : CategoryViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        binding = FragmentCategorySettingBinding.inflate(inflater, container, false)

        initObserve()
        onClickSchedule()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Log.d("CategorySettingFrag", "onResume()")
        getCategoryList()
        setAdapter()
    }

    private fun onClickSchedule() {
        // 닫힘 버튼 누르면 종료
        binding.categoryCloseTv.setOnClickListener {
            activity?.finish()
        }

        // 저장 버튼
        binding.categorySaveTv.setOnClickListener {
            activity?.finish()
        }

        //팔레트 설정
        binding.categoryCalendarPaletteSetting.setOnClickListener {

        }

        // 카테고리 추가
        onClickCategoryAddBtn()
    }

    private fun onClickCategoryAddBtn() {
        binding.categoryAddBtn.setOnClickListener { // 새 카테고리
            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.category_frm, CategoryDetailFragment(false))
                .addToBackStack(null) // 백 스택에 트랜잭션을 추가
                .commitAllowingStateLoss()
        }
    }

    private fun setAdapter() {
        Log.d("getCategories", "initRV")
        categoryRVAdapter = SetCategoryRVAdapter()
        binding.categoryCalendarRv.apply {
            adapter = categoryRVAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
        categoryRVAdapter.setCategoryClickListener(object: SetCategoryRVAdapter.MyItemClickListener {
            // 아이템 클릭
            override fun onItemClick(category: Category, position: Int) {
                Log.d("Category-Set-FRAG", "카테고리 아이템을 클릭했음")
                Log.e("SET-CATEGORY", "$category , $position")

                // 데이터 저장
                saveClickedData(category)
                categoryRVAdapter.notifyItemChanged(position)

                // 편집 화면으로 이동
                startActivity(Intent(requireActivity(), CategoryEditActivity()::class.java))
            }
        })
    }

    private fun initObserve() {
        viewModel.categoryList.observe(viewLifecycleOwner) {
            categoryList.clear()
            if (!it.isNullOrEmpty()) {
                categoryList = it as ArrayList<Category>
                categoryRVAdapter.addCategory(categoryList)
                Log.d("getCategories", "initObserve")
            }
        }
    }

    /** 카테고리 조회 */
    private fun getCategoryList() {
        lifecycleScope.launch{
            viewModel.getCategories()
        }
    }

    private fun saveClickedData(dataSet: Category) {
        // 클릭한 카테고리 데이터를 편집 화면으로 넘기기 위함
        val spf = requireActivity().getSharedPreferences(CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)
        val editor = spf.edit()
        val gson = Gson()
        val json = gson.toJson(dataSet) // 카테고리 데이터 변환
//        Log.d("Category", "categoryJson: $json")

        // spf에 저장
        editor
            .putString(CATEGORY_DATA, json)
            .putLong(CATEGORY_ID, dataSet.categoryId)
            .putLong(CATEGORY_SERVER_ID, dataSet.serverId)
            .apply()

        Log.d("debug", "Category Data saved")
    }

    override fun onGetAllCategorySuccess(response: GetCategoryResponse) {
        Log.d("CategorySettingFrag", "onGetAllCategorySuccess")
    }

    override fun onGetAllCategoryFailure(message: String) {
        Log.d("CategorySettingFrag", "onGetAllCategoryFailure")
    }

    companion object {
        const val CATEGORY_KEY_PREFS = "category"
        const val CATEGORY_DATA = "category_data"
        const val CATEGORY_ID = "categoryId"
        const val CATEGORY_SERVER_ID = "serverId"
    }
}