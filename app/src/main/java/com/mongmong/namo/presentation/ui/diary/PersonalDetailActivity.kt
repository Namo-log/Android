package com.mongmong.namo.presentation.ui.diary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.ActivityPersonalDiaryDetailBinding
import com.mongmong.namo.presentation.ui.diary.adapter.GalleryListAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryImageItemDecoration
import com.mongmong.namo.presentation.ui.group.diary.DiaryImageDetailActivity
import com.mongmong.namo.presentation.utils.PermissionChecker.hasImagePermission
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PersonalDetailActivity : AppCompatActivity(), ConfirmDialogInterface {
    private lateinit var binding: ActivityPersonalDiaryDetailBinding
    private lateinit var galleryAdapter: GalleryListAdapter

    private val viewModel : DiaryDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewModel = this@PersonalDetailActivity.viewModel
            lifecycleOwner = this@PersonalDetailActivity
            paletteId = intent.getIntExtra("paletteId", 0)
        }
        setSchedule()
        onClickListener()
        initRecyclerView()
        initObserve()
    }

    private fun setSchedule() {
        val schedule = (intent.getSerializableExtra("schedule") as? Schedule)!!
        hasDiary(schedule)
    }

    private fun hasDiary(schedule: Schedule) {
        if (schedule.hasDiary == false) {  // 기록 없을 때, 추가
            viewModel.setNewPersonalDiary(schedule)
        } else {  // 기록 있을 때, 수정
            viewModel.getExistingPersonalDiary(schedule)
        }
    }

    private fun onClickListener() {
        binding.apply {
            diaryBackIv.setOnClickListener { finish() }
            diaryGalleryClickIv.setOnClickListener { getGallery() }
            diaryEditBtnTv.setOnClickListener {
                lifecycleScope.launch {
                    if(this@PersonalDetailActivity.viewModel.schedule.value?.hasDiary == false) insertData()
                    else updateDiary()
                }
            }
            diaryDeleteIv.setOnClickListener {
                showDialog()
            }
        }
    }

    private fun initRecyclerView() {
        galleryAdapter = GalleryListAdapter(false,
            deleteClickListener = { newImages ->
                viewModel.updateImgList(newImages)
            },
            imageClickListener = {
                startActivity(Intent(this, DiaryImageDetailActivity::class.java))
            }
        )

        binding.diaryGallerySavedRv.apply {
            adapter = galleryAdapter.apply { addItemDecoration(DiaryImageItemDecoration(this@PersonalDetailActivity, IMAGE_MARGIN)) }
            layoutManager = LinearLayoutManager(this@PersonalDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    /** 다이어리 추가 **/
    private fun insertData() {
        if (viewModel.diary.value?.content.isNullOrEmpty() && viewModel.imgList.value.isNullOrEmpty()) {
            Snackbar.make(binding.root, "내용이나 이미지를 추가해주세요!", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            viewModel.addPersonalDiary()
        }
    }

    /** 다이어리 수정 **/
    private fun updateDiary() {
        viewModel.editPersonalDiary()
        Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show()
    }

    /** 다이어리 삭제 **/
    private fun deleteDiary() {
        viewModel.deletePersonalDiary()
    }

    private fun initObserve() {
        viewModel.diary.observe(this) { diary ->
            viewModel.updateImgList(diary.images?: emptyList())
        }
        viewModel.imgList.observe(this) {
            galleryAdapter.addImages(it)
        }
        viewModel.addDiaryResult.observe(this) { response ->
            if(response.code == OK) {
                finish()
            }
        }
        viewModel.editDiaryResult.observe(this) { response ->
            if(response.code == OK) {
                finish()
            }
        }
        viewModel.deleteDiaryResult.observe(this) { response ->
            // 다이어리 삭제 작업이 완료되었을 때 finish() 호출
            if (response.code == OK) {
                Toast.makeText(this, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showDialog() {
        // 삭제 확인 다이얼로그
        val title = "가록을 정말 삭제하시겠습니까?"

        val dialog = ConfirmDialog(this, title, null, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    /** 갤러리에서 이미지 가져오기 **/
    private fun getGallery() {
        if (hasImagePermission(this)) {
            val galleryIntent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                type = "image/*"
                data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기
            }
            getImage.launch(galleryIntent)
        } else {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
            ActivityCompat.requestPermissions(this, permissions, 200)
        }
    }
    //
    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val imageUris = getImageUrisFromResult(result)
        if(imageUris.isNullOrEmpty()) return@registerForActivityResult
        if (imageUris.size > 3) { // 사진 3장 이상 선택 시
            Toast.makeText(this, "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                .show()
            return@registerForActivityResult
        }
        viewModel.updateImgList(imageUris)
    }
    //
    private fun getImageUrisFromResult(result: ActivityResult): List<String> {
        if (result.resultCode != Activity.RESULT_OK) return emptyList()

        return result.data?.let { data ->
            mutableListOf<String>().apply {
                // 다중 선택 처리
                data.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) { // 최대 3개
                        add(clipData.getItemAt(i).uri.toString())
                    }
                } ?: data.data?.let { uri ->
                    // 단일 선택 처리
                    add(uri.toString())
                }
            }
        } ?: emptyList() // 결과 데이터가 null인 경우 빈 리스트 반환
    }


    override fun onClickYesButton(id: Int) {
        // 삭제 버튼 누르면 삭제 진행
        deleteDiary()
    }

    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val OK = 200
        const val IMAGE_MARGIN = 2
    }
}

