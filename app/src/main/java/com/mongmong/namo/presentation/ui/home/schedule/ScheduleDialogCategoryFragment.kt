package com.mongmong.namo.presentation.ui.home.schedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.presentation.ui.home.schedule.adapter.DialogCategoryRVAdapter
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.FragmentScheduleDialogCategoryBinding
import com.mongmong.namo.presentation.ui.category.CategoryActivity
import com.mongmong.namo.presentation.ui.category.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScheduleDialogCategoryFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogCategoryBinding

    private val args : ScheduleDialogCategoryFragmentArgs by navArgs()

    private var schedule : Schedule = Schedule()

    private lateinit var categoryRVAdapter : DialogCategoryRVAdapter
    private var categoryList : List<Category> = arrayListOf()
    private var selectedCategory : Long = 0

    private val viewModel : CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleDialogCategoryBinding.inflate(inflater, container, false)
        Log.d("DIALOG_CATEGORY", "카테고리 다이얼로그")

        schedule = args.schedule

        selectedCategory = schedule.categoryId

        onClickCategoryEdit()
        initObserve()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getCategoryList()
    }

    private fun onClickCategoryEdit()  {
        binding.dialogScheduleCategoryEditCv.setOnClickListener {
            Log.d("DialogCategoryFrag", "categoryEditCV 클릭")
            startActivity(Intent(activity, CategoryActivity::class.java))
        }
    }

    private fun setAdapter() {
        categoryRVAdapter = DialogCategoryRVAdapter(categoryList)
        categoryRVAdapter.setSelectedId(selectedCategory)
        categoryRVAdapter.setMyItemClickListener(object: DialogCategoryRVAdapter.MyItemClickListener {
            // 아이템 클릭
            override fun onSendId(category: Category) {
                // 카테고리 세팅
                schedule.categoryId = category.categoryId
                schedule.categoryServerId = category.serverId
                Log.d("TEST_CATEGORY", "In category : ${schedule.categoryId}")
                Log.d("TEST_CATEGORY", "In category Server: ${schedule.categoryServerId}")
                Log.d("TEST_CATEGORY", "In category Result: ${schedule}")
                val action = ScheduleDialogCategoryFragmentDirections.actionScheduleDialogCategoryFragmentToScheduleDialogBasicFragment(schedule)
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
                categoryList = it
                setAdapter()
            }
        }
    }

    /** 카테고리 조회 */
    private fun getCategoryList() {
        lifecycleScope.launch{
            viewModel.getCategories()
        }
    }
}