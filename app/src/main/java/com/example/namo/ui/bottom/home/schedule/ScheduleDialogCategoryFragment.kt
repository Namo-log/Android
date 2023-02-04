package com.example.namo.ui.bottom.home.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.ui.bottom.home.schedule.data.Category
import com.example.namo.databinding.FragmentScheduleDialogCategoryBinding

class ScheduleDialogCategoryFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogCategoryBinding
    private val categoryRVAdapter : DialogCategoryRVAdapter = DialogCategoryRVAdapter()

    private val categoryList : ArrayList<Category> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleDialogCategoryBinding.inflate(inflater, container, false)
        Log.d("DIALOG_CATEGORY", "카테고리 다이얼로그")

        setAdapter()

        return binding.root
    }

    private fun setAdapter() {
        binding.scheduleDialogCategoryRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.scheduleDialogCategoryRv.adapter = categoryRVAdapter
        Log.d("CATEGORY_BEFORE", categoryList.toString())
        getCategoryList()
        Log.d("CATEGORY_AFTER", categoryList.toString())
        categoryRVAdapter.addCategory(categoryList)
        categoryRVAdapter.notifyDataSetChanged()
    }

    private fun getCategoryList() {
        categoryList.apply {
            add(
                Category(
                    "카테고리1",
                    R.color.palette1,
                    false
                )
            )
            add(
                Category(
                    "카테고리2",
                    R.color.palette2,
                    false
                )
            )
            add(
                Category(
                    "카테고리3",
                    R.color.palette3,
                    false
                )
            )
            add(
                Category(
                    "카테고리4",
                    R.color.palette4,
                    false
                )
            )
            add(
                Category(
                    "카테고리5",
                    R.color.palette5,
                    false
                )
            )
        }
    }
}