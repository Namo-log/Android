package com.mongmong.namo.presentation.ui.category

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityCategoryEditBinding
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.utils.ConfirmDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryEditActivity : AppCompatActivity(), ConfirmDialog.ConfirmDialogInterface {

    lateinit var binding: ActivityCategoryEditBinding

    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewModel = this@CategoryEditActivity.viewModel
            lifecycleOwner = this@CategoryEditActivity
        }

        loadPref()
        initClickListeners()
        initObservers()

        supportFragmentManager.beginTransaction()
            .replace(R.id.category_edit_frm, CategoryDetailFragment(true))
            .commitAllowingStateLoss()
    }

    private fun initClickListeners() {
        // 다크뷰 클릭 시 화면 종료
//        binding.categoryDarkView.setOnClickListener {
//            finish()
//        }

        // 카테고리 삭제 진행
        binding.categoryDeleteBtn.setOnClickListener {
            if (viewModel.canDeleteCategory.value == false) {
                Toast.makeText(this, "기본 카테고리는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 다이얼로그
            val title = "카테고리를 삭제하시겠어요?"
            val content = "삭제하더라도 카테고리에\n포함된 일정은 사라지지 않습니다."

            val dialog = ConfirmDialog(this@CategoryEditActivity, title, content, "삭제", 0)
            dialog.isCancelable = false
            dialog.show(this.supportFragmentManager, "ConfirmDialog")
        }
    }

    private fun loadPref() {
        // 삭제 여부 체크
        viewModel.setDeliable(intent.getBooleanExtra("canDelete", false))
        // roomDB
        val spf = getSharedPreferences(CategorySettingFragment.CATEGORY_KEY_PREFS, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = spf.getString(CategorySettingFragment.CATEGORY_DATA, "")
        try {
            // 데이터에 타입을 부여하기 위한 typeToken
            val typeToken = object : TypeToken<Category>() {}.type
            // 데이터 받기
            viewModel.setCategory(gson.fromJson(json, typeToken))
        } catch (e: JsonParseException) { // 파싱이 안 될 경우
            e.printStackTrace()
        }
    }

    private fun initObservers() {
        viewModel.isComplete.observe(this) { isComplete ->
            // 삭제 작업이 완료된 후 뒤로가기
            if (isComplete) {
                viewModel.completeState.observe(this) { state ->
                    when(state) {
                        RoomState.DELETED -> Toast.makeText(this, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        else -> {}
                    }
                }
                finish()
            }
        }
    }

    private fun deleteCategory() {
        //TODO: 기본 카테고리 삭제 불가 처리
//        category.isUpload = UploadState.IS_NOT_UPLOAD.state
//        category.state = RoomState.DELETED.state
        viewModel.deleteCategory()

        // 서버 통신
//            uploadToServer(RoomState.DELETED.state)
    }

    override fun onClickYesButton(id: Int) {
        // 삭제 버튼 클릭하면 삭제 진행
        lifecycleScope.launch {
            deleteCategory()
        }
    }

}