package com.example.namo.ui.bottom.home.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.R
import com.example.namo.config.BaseResponse
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.ActivityCategoryEditBinding
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.category.CategoryDeleteService
import com.example.namo.data.remote.category.CategoryDeleteView
import com.example.namo.data.remote.category.PostCategoryResponse
import com.example.namo.utils.NetworkManager

class CategoryEditActivity : AppCompatActivity(), CategoryDeleteView {

    lateinit var binding: ActivityCategoryEditBinding

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    var categoryId: Long = -1
    var serverId: Long = 0

    private val failList = ArrayList<Category>()

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
        }
    }

    private fun deleteCategory() {
        // roomDB
        val spf = getSharedPreferences(CategorySettingFragment.CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)
        categoryId = spf.getLong(CategorySettingFragment.CATEGORY_KEY_IDX, -1)
        serverId = spf.getLong(CategorySettingFragment.CATEGORY_KEY_SERVER_IDX, -1)

        if (categoryId == 1L || categoryId == 2L) {
            Toast.makeText(this, "기본 카테고리는 삭제할 수 없습니다", Toast.LENGTH_SHORT).show()
        } else {
            // 서버 통신
            uploadToServer(R.string.event_current_deleted.toString())
            Thread{
                category = db.categoryDao.getCategoryWithId(categoryId)
                // 삭제 대신 비활성화 처리
                db.categoryDao.updateCategory(category.copy(active = false))
//                db.categoryDao.deleteCategory(category)
                Log.d("CategoryEditActivity", "deleteCategory: $category")
            }.start()
            finish()

            // 서버 통신
            uploadToServer(R.string.event_current_deleted.toString())
        }
//        CategoryDeleteService(this).tryDeleteCategory(7)
    }

    private fun uploadToServer(state : String) {
        // 룸디비에 isUpload, serverId, state 업데이트하기
        val thread = Thread {
            category = db.categoryDao.getCategoryWithId(categoryId)
            db.categoryDao.updateCategoryAfterUpload(categoryId, 0, category.serverIdx, state)
            failList.clear()
            failList.addAll(db.categoryDao.getNotUploadedCategory() as ArrayList<Category>)
        }
        thread.start()
        try {
            thread.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }

        if (!NetworkManager.checkNetworkState(this)) {
            // 인터넷 연결 안 됨
            Log.d("CategoryEditActivity", "WIFI ERROR : $failList")
            return
        }

        CategoryDeleteService(this).tryDeleteCategory(serverId)
    }

    private fun updateCategoryAfterUpload(state: String) {

        when (state) {
            // 서버 통신 성공
            R.string.event_current_default.toString() -> {
                val thread = Thread {
                    db.categoryDao.updateCategoryAfterUpload(categoryId, 1, serverId, state)
                }
                thread.start()
                try {
                    thread.join()
                } catch ( e: InterruptedException) {
                    e.printStackTrace()
                }
                Log.e("CategoryEditAct", "serverId 업데이트 성공, serverId: ${category.serverIdx}, categoryId: ${categoryId}")
            }
            // 서버 업로드 실패
            else -> {
                val thread = Thread {
                    db.categoryDao.updateCategoryAfterUpload(categoryId, 0, serverId, state)
                    failList.clear()
                    failList.addAll(db.categoryDao.getNotUploadedCategory() as ArrayList<Category>)
                }
                thread.start()
                try {
                    thread.join()
                } catch ( e: InterruptedException) {
                    e.printStackTrace()
                }
                Log.d("CategoryEditAct", "Server Fail : $failList")
            }
        }

//        val thread = Thread {
//            db.categoryDao.updateCategoryAfterUpload(categoryId, 1, result.categoryId, state)
//        }
    }

    override fun onDeleteCategorySuccess(response: BaseResponse) {
        Log.d("CategoryEditAct", "onDeleteCategorySuccess")
        // 룸디비에 isUpload, serverId, state 업데이트하기
        updateCategoryAfterUpload(R.string.event_current_default.toString())
    }

    override fun onDeleteCategoryFailure(message: String) {
        Log.d("CategoryEditAct", "onDeleteCategoryFailure")

        // 룸디비에 failList 업데이트하기
        updateCategoryAfterUpload(R.string.event_current_deleted.toString())
    }

}