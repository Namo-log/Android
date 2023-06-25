package com.example.namo.ui.bottom.home.schedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentScheduleDialogCategoryBinding
import com.example.namo.ui.bottom.home.category.CategoryActivity

class ScheduleDialogCategoryFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogCategoryBinding
    private lateinit var db: NamoDatabase
    private val args : ScheduleDialogCategoryFragmentArgs by navArgs()

    private var event : Event = Event()

    private lateinit var categoryRVAdapter : DialogCategoryRVAdapter
    private var categoryList : List<Category> = arrayListOf()
    private var initCategory : Int = 0
    private var selectedCategory : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleDialogCategoryBinding.inflate(inflater, container, false)
        Log.d("DIALOG_CATEGORY", "카테고리 다이얼로그")

        db = NamoDatabase.getInstance(requireContext())
        event = args.event

        selectedCategory = event.categoryIdx

        onClickCategoryEdit()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getCategoryList()
    }

    private fun getCategoryList() {
        // 카테고리가 아무것도 없으면 기본 카테고리 2개 생성 (일정, 모임)
        setInitialCategory()

        val rv = binding.dialogScheduleCategoryRv

        val r = Runnable {
            try {
                categoryList = db.categoryDao.getActiveCategoryList(true)
                categoryRVAdapter = DialogCategoryRVAdapter(requireContext(), categoryList)
                categoryRVAdapter.setSelectedIdx(selectedCategory)
                categoryRVAdapter.setMyItemClickListener(object: DialogCategoryRVAdapter.MyItemClickListener {
                    // 아이템 클릭
                    override fun onSendIdx(category: Category) {
                        // 카테고리 세팅
                        selectedCategory = category.categoryIdx

                        event.categoryIdx = category.categoryIdx
                        event.categoryName = category.name
                        event.categoryColor = category.color

                        val action = ScheduleDialogCategoryFragmentDirections.actionScheduleDialogCategoryFragmentToScheduleDialogBasicFragment(event)
                        findNavController().navigate(action)
                    }
                })
                // 활성화 상태인 리스트만 보줌
                categoryList = db.categoryDao.getActiveCategoryList(true)
                requireActivity().runOnUiThread {
                    rv.adapter = categoryRVAdapter
                    rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                }
                Log.d("ScheduleDialogFrag", "categoryDao: ${db.categoryDao.getCategoryList()}")
            } catch (e: Exception) {
                Log.d("schedule category", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun setInitialCategory() {
        // 리스트에 아무런 카테고리가 없으면 기본 카테고리 설정
        val thread = Thread {
            if (db.categoryDao.getCategoryList().isEmpty()) {
                db.categoryDao.insertCategory(Category(0, "일정", R.color.schedule, true))
                db.categoryDao.insertCategory(Category(0, "그룹", R.color.schedule_group, true))
            }
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun onClickCategoryEdit()  {
        binding.dialogScheduleCategoryEditCv.setOnClickListener {
            Log.d("DialogCategoryFrag", "categoryEditCV 클릭")
            startActivity(Intent(activity, CategoryActivity::class.java))
        }
    }
}