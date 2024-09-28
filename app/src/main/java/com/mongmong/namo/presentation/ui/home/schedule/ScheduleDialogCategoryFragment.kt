package com.mongmong.namo.presentation.ui.home.schedule

import android.content.Intent
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.presentation.ui.home.schedule.adapter.DialogCategoryRVAdapter
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.databinding.FragmentScheduleDialogCategoryBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.category.CategoryActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleDialogCategoryFragment
    : BaseFragment<FragmentScheduleDialogCategoryBinding>(R.layout.fragment_schedule_dialog_category) {

    private lateinit var categoryRVAdapter: DialogCategoryRVAdapter

    private val viewModel: PersonalScheduleViewModel by activityViewModels()

    override fun setup() {
        binding.viewModel = viewModel

        onClickCategoryEdit()
        initObserve()
    }

    private fun onClickCategoryEdit()  {
        binding.dialogScheduleCategoryEditCv.setOnClickListener {
            Log.d("DialogCategoryFrag", "categoryEditCV 클릭")
            startActivity(Intent(activity, CategoryActivity::class.java))
        }
    }

    private fun setAdapter(categoryList: List<Category>) {
        categoryRVAdapter = DialogCategoryRVAdapter(categoryList)
        categoryRVAdapter.setSelectedId(viewModel.schedule.value!!.categoryInfo.categoryId)
        categoryRVAdapter.setMyItemClickListener(object: DialogCategoryRVAdapter.MyItemClickListener {
            // 아이템 클릭
            override fun onSendId(category: Category) {
                // 카테고리 세팅
                viewModel.updateCategory(category)
                val action = ScheduleDialogCategoryFragmentDirections.actionScheduleDialogCategoryFragmentToScheduleDialogBasicFragment()
                Log.d("CategoryFragment", "selected category: $category")
                findNavController().navigate(action)
            }
        })
        binding.dialogScheduleCategoryRv.apply {
            adapter = categoryRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun initObserve() {
        viewModel.categoryList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                setAdapter(it)
            }
        }
    }
}