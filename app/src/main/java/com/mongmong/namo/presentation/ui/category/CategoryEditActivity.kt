package com.mongmong.namo.presentation.ui.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.databinding.ActivityCategoryEditBinding
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.NetworkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryEditActivity : AppCompatActivity(), ConfirmDialogInterface {

    lateinit var binding: ActivityCategoryEditBinding

    private lateinit var db: NamoDatabase
    private lateinit var category: Category

    var categoryId: Long = -1
    var serverId: Long = 0

    private val failList = ArrayList<Category>()

    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NamoDatabase.getInstance(this)

        loadPref()
        onClickListener()

        supportFragmentManager.beginTransaction()
            .replace(R.id.category_edit_frm, CategoryDetailFragment(true))
            .commitAllowingStateLoss()

        initObservers()
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

    private fun loadPref() {
        // roomDB
        val spf = getSharedPreferences(CategorySettingFragment.CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = spf.getString(CategorySettingFragment.CATEGORY_DATA, "")
        try {
            // 데이터에 타입을 부여하기 위한 typeToken
            val typeToken = object : TypeToken<Category>() {}.type
            // 데이터 받기
            category = gson.fromJson(json, typeToken)
        } catch (e: JsonParseException) { // 파싱이 안 될 경우
            e.printStackTrace()
        }
//        categoryId = spf.getLong(CategorySettingFragment.CATEGORY_ID, -1)
//        serverId = spf.getLong(CategorySettingFragment.CATEGORY_SERVER_ID, -1)
    }

    private fun initObservers() {
        viewModel.category.observe(this) {
            //
        }
    }

    private fun deleteCategory() {

        if (categoryId == 1L || categoryId == 2L) {
            Toast.makeText(this, "기본 카테고리는 삭제할 수 없습니다", Toast.LENGTH_SHORT).show()
        } else {
            category.isUpload = UploadState.IS_NOT_UPLOAD.state
            category.state = RoomState.DELETED.state
            viewModel.deleteCategory(category)
            Toast.makeText(this, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            finish()

            // 서버 통신
//            uploadToServer(RoomState.DELETED.state)
        }
    }

    private fun uploadToServer(state : String) {
        // 룸디비에 isUpload, serverId, state 업데이트하기
        val thread = Thread {
            category = db.categoryDao.getCategoryWithId(categoryId)
            viewModel.updateCategoryAfterUpload(categoryId, UploadState.IS_NOT_UPLOAD.state, category.categoryId, RoomState.DEFAULT.state)
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

//        CategoryDeleteService(this).tryDeleteCategory(serverId, categoryId)
    }

    override fun onClickYesButton(id: Int) {
        // 삭제 버튼 클릭하면 삭제 진행
        lifecycleScope.launch {
            deleteCategory()
        }
    }

}