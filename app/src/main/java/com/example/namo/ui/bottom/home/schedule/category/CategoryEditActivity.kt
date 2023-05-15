package com.example.namo.ui.bottom.home.schedule.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.ActivityCategoryEditBinding
import com.example.namo.ui.bottom.diary.adapter.GalleryListAdapter
import com.example.namo.ui.bottom.home.schedule.data.Category
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

class CategoryEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityCategoryEditBinding

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    var categoryIdx = -1
    var name = ""
    var color = 0
    var share = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NamoDatabase.getInstance(this)

        onClickListener()

        supportFragmentManager.beginTransaction()
            .replace(R.id.category_edit_frm, CategoryDetailFragment(true))
            .commitAllowingStateLoss()
    }

    private fun onClickListener() {

        // 다크뷰 클릭 시 화면 종료
//        binding.categoryDarkView.setOnClickListener {
//            finish()
//        }

        // 카테고리 삭제 진행
        binding.categoryDeleteIv.setOnClickListener {
            Log.d("CategoryEditActivity", "카테고리삭제 클릭")
            deleteCategory()
            finish()
        }
    }

    private fun deleteCategory() {
        val spf = getSharedPreferences(CategorySettingFragment.CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)
        categoryIdx = spf.getInt(CategorySettingFragment.CATEGORY_KEY_IDX, -1)

        Thread{
            category = db.categoryDao.getCategoryContent(categoryIdx)
            db.categoryDao.deleteCategory(category)
            Log.d("CategoryEditActivity", "deleteCategory: $category")
        }.start()
    }

}