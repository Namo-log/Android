package com.gradu.presentation.ui.home.schedule

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentScheduleDialogCategoryBinding
import com.mongmong.namo.domain.model.CategoryModel
import com.mongmong.namo.presentation.config.BaseFragment
import com.gradu.presentation.ui.home.schedule.adapter.DialogCategoryRVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleDialogCategoryFragment
    : BaseFragment<FragmentScheduleDialogCategoryBinding>(R.layout.fragment_schedule_dialog_category) {

    private lateinit var categoryRVAdapter: DialogCategoryRVAdapter

    private val viewModel: PersonalScheduleViewModel by activityViewModels()

    override fun setup() {
        binding.viewModel = viewModel

        viewModel.setDeleteBtnVisibility(false)
        initClickListeners()
        initObserve()
    }

    override fun onResume() {
        super.onResume()

        viewModel.getCategories()
    }

    private fun initClickListeners()  {
        // 뒤로가기 (일정 화면)
        binding.dialogScheduleCategoryBackIv.setOnClickListener {
            val action = ScheduleDialogCategoryFragmentDirections.actionScheduleDialogCategoryFragmentToScheduleDialogBasicFragment()
            findNavController().navigate(action)
        }

        // 카테고리 추가
        binding.dialogScheduleCategoryAddBtn.setOnClickListener {
            findNavController().navigate(R.id.action_scheduleDialogCategoryFragment_to_categoryFragment)
        }
    }

    private fun setAdapter(categoryList: List<CategoryModel>) {
        categoryRVAdapter = DialogCategoryRVAdapter(categoryList)
        categoryRVAdapter.setSelectedId(viewModel.schedule.value!!.categoryInfo.categoryId)
        categoryRVAdapter.setMyItemClickListener(object: DialogCategoryRVAdapter.MyItemClickListener {
            // 카테고리 선택 진행
            override fun onSelectCategory(category: CategoryModel) {
                viewModel.updateCategory(category)
                val action = ScheduleDialogCategoryFragmentDirections.actionScheduleDialogCategoryFragmentToScheduleDialogBasicFragment()
                findNavController().navigate(action)
            }

            // 카테고리 편집 화면으로 이동
            override fun onEditCategory(category: CategoryModel) {
                val action = ScheduleDialogCategoryFragmentDirections.actionScheduleDialogCategoryFragmentToCategoryFragment(category)
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