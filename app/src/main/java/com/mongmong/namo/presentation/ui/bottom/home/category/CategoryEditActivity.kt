package com.mongmong.namo.presentation.ui.bottom.home.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.databinding.ActivityCategoryEditBinding
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.remote.category.CategoryDeleteService
import com.mongmong.namo.data.remote.category.CategoryDeleteView
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.NetworkManager

class CategoryEditActivity : AppCompatActivity(), ConfirmDialogInterface, CategoryDeleteView {

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
            // 다이얼로그
            val title = "카테고리를 삭제하시겠어요?"
            val content = "삭제하더라도 카테고리에\n포함된 일정은 사라지지 않습니다."

            val dialog = ConfirmDialog(this@CategoryEditActivity, title, content, "삭제", 0)
            dialog.isCancelable = false
            dialog.show(this.supportFragmentManager, "ConfirmDialog")
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
            Toast.makeText(this, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
            db.categoryDao.updateCategoryAfterUpload(categoryId, 0, category.serverId, state)
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

        CategoryDeleteService(this).tryDeleteCategory(serverId, categoryId)
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
                Log.e("CategoryEditAct", "serverId 업데이트 성공, serverId: ${category.serverId}, categoryId: ${categoryId}")
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

    override fun onDeleteCategorySuccess(response: BaseResponse, categoryId : Long) {
        Log.d("CategoryEditAct", "onDeleteCategorySuccess")
        // 룸디비에 isUpload, serverId, state 업데이트하기
        updateCategoryAfterUpload(R.string.event_current_default.toString())
    }

    override fun onDeleteCategoryFailure(message: String) {
        Log.d("CategoryEditAct", "onDeleteCategoryFailure")

        // 룸디비에 failList 업데이트하기
        updateCategoryAfterUpload(R.string.event_current_deleted.toString())
    }

    override fun onClickYesButton(id: Int) {
        // 삭제 버튼 클릭하면 삭제 진행
        deleteCategory()
    }

}