package com.mongmong.namo.presentation.ui.diary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.ActivityPersonalDiaryDetailBinding
import com.mongmong.namo.presentation.ui.diary.adapter.GalleryImageRVAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.utils.PermissionChecker.hasImagePermission
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class PersonalDiaryDetailActivity
    : BaseActivity<ActivityPersonalDiaryDetailBinding>(R.layout.activity_personal_diary_detail),
    ConfirmDialogInterface {
    private lateinit var galleryAdapter: GalleryImageRVAdapter

    private val viewModel : DiaryDetailViewModel by viewModels()

    override fun setup() {
        binding.apply {
            viewModel = this@PersonalDiaryDetailActivity.viewModel
            paletteId = intent.getIntExtra("paletteId", 0)

            // marquee focus
            diaryTitleTv.requestFocus()
            diaryTitleTv.isSelected = true
        }
        setScheduleData()
        onClickListener()
        initRecyclerView()
        initObserve()
    }

    private fun setScheduleData() {
        viewModel.setSchedule(
            scheduleId = intent.getLongExtra("scheduleId", 0),
            title = intent.getStringExtra("title") ?: "",
            date = intent.getStringExtra("scheduleDate") ?: "",
            place = intent.getStringExtra("scheduleDate") ?: "",
            hasDiary = intent.getBooleanExtra("hasDiary", false)
        )
        //val place = intent.getStringExtra("place")
        setCreateOrEdit()
    }

    private fun setCreateOrEdit() {
        if (viewModel.diarySchedule.value?.hasDiary == false) {  // 기록 없을 때, 추가
            viewModel.setNewDiary()
        } else {  // 기록 있을 때, 수정
            viewModel.getPersonalDiary()
        }
    }

    private fun onClickListener() {
        with(binding) {
            onBackPressedDispatcher.addCallback(this@PersonalDiaryDetailActivity, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(this@PersonalDiaryDetailActivity.viewModel.isDiaryChanged()) { showBackDialog() }
                    else finish()
                }
            })

            diaryBackIv.setOnClickListener {
                if (this@PersonalDiaryDetailActivity.viewModel.isDiaryChanged()) { showBackDialog() }
                else finish()
            }
            diaryGalleryClickIv.setOnClickListener { getGallery() }
            diaryEditBtnTv.setOnClickListener {
                lifecycleScope.launch {
                    if(this@PersonalDiaryDetailActivity.viewModel.schedule.value?.hasDiary == false) insertData()
                    else updateDiary()
                }
            }
            diaryDeleteIv.setOnClickListener {
                showDeleteDialog()
            }
        }
    }

    private fun initRecyclerView() {
        galleryAdapter = GalleryImageRVAdapter(false,
            deleteClickListener = { removedImage ->
                viewModel.removeImage(removedImage)
            },
            imageClickListener = {
                startActivity(
                    Intent(this, DiaryImageDetailActivity::class.java).apply {
                        putExtra("imgs", ArrayList(viewModel.imgList.value))
                    }
                )
            }
        )

        binding.diaryGallerySavedRv.apply {
            adapter = galleryAdapter.apply { addItemDecoration(DiaryImageItemDecoration(this@PersonalDiaryDetailActivity, IMAGE_MARGIN)) }
            layoutManager = LinearLayoutManager(this@PersonalDiaryDetailActivity, LinearLayoutManager.HORIZONTAL, false)
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
            viewModel.updateImgList(diary.diaryImages ?: emptyList())
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

    /** 삭제 확인 다이얼로그 */
    private fun showDeleteDialog() {
        val title = "기록을 정말 삭제하시겠습니까?"

        val dialog = ConfirmDialog(this, title, null, "삭제", DELETE_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    /** 뒤로가기 확인 다이얼로그 */
    private fun showBackDialog() {
        val title = "편집한 내용이 저장되지 않습니다."
        val content = "정말 나가시겠어요?"

        val dialog = ConfirmDialog(this, title, content, "확인", BACK_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    override fun onClickYesButton(id: Int) {
        when(id) {
            DELETE_BUTTON_ACTION -> deleteDiary() // 삭제
            BACK_BUTTON_ACTION -> finish() // 뒤로가기
        }
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

    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val imageUris = getImageUrisFromResult(result)
        val currentImageCount = viewModel.imgList.value?.size ?: 0
        if (imageUris.isNullOrEmpty()) return@registerForActivityResult
        if (currentImageCount + imageUris.size > 3) {
            Toast.makeText(this, "사진은 총 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addCreateImages(imageUris)
        }
    }

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

    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val OK = 200
        const val IMAGE_MARGIN = 2
        const val DELETE_BUTTON_ACTION = 1
        const val BACK_BUTTON_ACTION = 2
    }
}
