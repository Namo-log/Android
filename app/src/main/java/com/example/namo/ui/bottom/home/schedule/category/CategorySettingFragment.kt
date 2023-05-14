package com.example.namo.ui.bottom.home.schedule.category

import android.content.Context
import android.content.Intent
import android.graphics.Insets.add
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.FragmentCategorySettingBinding
import com.example.namo.ui.bottom.diary.adapter.DiaryListRVAdapter
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.ui.bottom.home.schedule.data.Category
import com.google.gson.Gson

class CategorySettingFragment: Fragment() {

    lateinit var binding: FragmentCategorySettingBinding //플로팅 카테고리 설정 화면

    private lateinit var categoryRVAdapter: SetCategoryRVAdapter

    private lateinit var db: NamoDatabase

    private var categoryList = arrayListOf<Category>() // : List<Category> = arrayListOf()

    private var gson: Gson = Gson()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        binding = FragmentCategorySettingBinding.inflate(inflater, container, false)

        db = NamoDatabase.getInstance(requireContext())
        categoryRVAdapter= SetCategoryRVAdapter(requireContext(), categoryList)

        //db initial insert
        initialCategory()

        // 닫힘 버튼 누르면 종료
        binding.categoryCloseTv.setOnClickListener {
            activity?.finish()
        }

        // 저장 버튼
        binding.categoryDetailSaveTv.setOnClickListener {
            activity?.finish()
        }

        //팔레트 설정
        binding.categoryCalendarPaletteSetting.setOnClickListener {

        }

        setAdapter()

        return binding.root
    }

    private fun setAdapter() {
        binding.categoryCalendarRv.layoutManager = GridLayoutManager(context, 2)
        binding.categoryCalendarRv.adapter = categoryRVAdapter
        Log.d("CATEGORY_BEFORE", categoryList.toString())
//        getCategoryList(categoryRVAdapter)
        Log.d("CATEGORY_AFTER", categoryList.toString())
//        categoryRVAdapter.addCategory(categoryList)
        categoryRVAdapter.notifyDataSetChanged()
    }


//    private fun getCategoryList(categoryRVAdapter: SetCategoryRVAdapter) {
//        //db initial insert
//        initialCategory()
//
//        val r = Runnable {
//            try {
//                categoryList = db.categoryDao.getCategoryList()
//                requireActivity().runOnUiThread {
//                    binding.categoryCalendarRv.adapter = categoryRVAdapter
//                }
//            } catch (e: Exception) {
//                Log.d("category", "Error - $e")
//            }
//        }
//
//        val thread = Thread(r)
//        thread.start()
//    }

    private fun initialCategory() {
        categoryList.apply {
            add(Category(0, "카테고리1", R.color.palette1, false))
            add(Category(1, "카테고리2", R.color.palette2, false))
            add(Category(2, "카테고리3", R.color.palette3, false))
            add(Category(3, "카테고리4", R.color.palette4, false))
            add(Category(4, "가테고리5", R.color.palette5, false))
        }
    }

    private fun initialCategory2() {
        Thread {
            db.categoryDao.insertCategory(Category(0, "카테고리1", R.color.palette1, false))
            db.categoryDao.insertCategory(Category(0, "카테고리2", R.color.palette2, false))
            db.categoryDao.insertCategory(Category(0, "카테고리3", R.color.palette3, false))
            db.categoryDao.insertCategory(Category(0, "카테고리4", R.color.palette4, false))
            db.categoryDao.insertCategory(Category(0, "카테고리5", R.color.palette5, false))
        }.start()
    }
}