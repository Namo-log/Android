package com.example.namo.ui.bottom.home.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.dao.CategoryDao
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.ui.bottom.home.schedule.data.Category
import com.example.namo.databinding.FragmentScheduleDialogCategoryBinding

class ScheduleDialogCategoryFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogCategoryBinding
    private val categoryRVAdapter : DialogCategoryRVAdapter = DialogCategoryRVAdapter()
    private lateinit var db: NamoDatabase

    private var categoryList : List<Category> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleDialogCategoryBinding.inflate(inflater, container, false)
        Log.d("DIALOG_CATEGORY", "카테고리 다이얼로그")

        db = NamoDatabase.getInstance(requireContext())

        setAdapter()

        return binding.root
    }

    private fun setAdapter() {
        binding.scheduleDialogCategoryRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        binding.scheduleDialogCategoryRv.adapter = categoryRVAdapter
        Log.d("CATEGORY_BEFORE", categoryList.toString())
        getCategoryList(categoryRVAdapter)
        Log.d("CATEGORY_AFTER", categoryList.toString())
//        categoryRVAdapter.addCategory(categoryList)
        categoryRVAdapter.notifyDataSetChanged()
    }

    private fun getCategoryList(categoryRVAdapter: DialogCategoryRVAdapter) {
        //db initial insert
        initialCategory()

        val r = Runnable {
            try {
                categoryList = db.categoryDao.getCategoryList()
                requireActivity().runOnUiThread {
                    binding.scheduleDialogCategoryRv.adapter = categoryRVAdapter
                }
            } catch (e: Exception) {
                Log.d("category", "Error - $e")
            }
        }
    }

    private fun initialCategory() {
        Thread(Runnable {
            db.categoryDao.insertCategory(Category(0, "카테고리1", R.color.palette1))
            db.categoryDao.insertCategory(Category(0, "카테고리2", R.color.palette2))
            db.categoryDao.insertCategory(Category(0, "카테고리3", R.color.palette3))
            db.categoryDao.insertCategory(Category(0, "카테고리4", R.color.palette4))
            db.categoryDao.insertCategory(Category(0, "가테고리5", R.color.palette5))
        }).start()
    }
}