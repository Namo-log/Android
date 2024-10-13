package com.mongmong.namo.presentation.ui.diary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.databinding.ActivityPersonalDiaryDetailBinding
import com.mongmong.namo.presentation.ui.diary.adapter.PersonalDiaryImagesRVAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.utils.PermissionChecker.hasImagePermission
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class PersonalDiaryDetailActivity
    : BaseActivity<ActivityPersonalDiaryDetailBinding>(R.layout.activity_personal_diary_detail),
    ConfirmDialogInterface {
    private lateinit var galleryAdapter: PersonalDiaryImagesRVAdapter

    private val viewModel: PersonalDiaryViewModel by viewModels()

    override fun setup() {
        binding.apply {
            viewModel = this@PersonalDiaryDetailActivity.viewModel

            // marquee focus
            diaryTitleTv.requestFocus()
            diaryTitleTv.isSelected = true
        }
        setScheduleData()
        initObserve()
        initClickListener()
        initRecyclerView()
    }

    private fun setScheduleData() {
        viewModel.getScheduleForDiary(intent.getLongExtra("scheduleId", 0))
        setCreateOrEdit()
    }

    private fun setCreateOrEdit() {
        if (viewModel.diarySchedule.value?.hasDiary == false) {
            Log.d("setCreateOrEdit", "dd")
            viewModel.setNewDiary()
        } else {  // 기록 있을 때, 수정
            Log.d("setCreateOrEdit", "dd2")
            viewModel.getDiaryData()
        }
    }

    private fun initClickListener() {
        onBackPressedDispatcher.addCallback(
            this@PersonalDiaryDetailActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.diaryChanged.value == true) {
                        showBackDialog()
                    } else finish()
                }
            })

        binding.diaryBackIv.setOnClickListener {
            if (viewModel.diaryChanged.value == true) {
                showBackDialog()
            } else finish()
        }
        binding.diaryGalleryClickIv.setOnClickListener { getGallery() }
        binding.diarySaveBtn.setOnClickListener {
            Log.d("PersonalDiaryDetailActivity", "save btn")
            if (viewModel.diarySchedule.value?.hasDiary == false) {
                Log.d("PersonalDiaryDetailActivity", "save btn add")
                viewModel.addDiary()
            } else {
                Log.d("PersonalDiaryDetailActivity", "save btn edit")
                viewModel.editDiary()
            }
        }
        binding.diaryDeleteIv.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun initRecyclerView() {
        galleryAdapter = PersonalDiaryImagesRVAdapter(
            deleteClickListener = { removedImage ->
                viewModel.removeImage(removedImage)
            },
            imageClickListener = {
                startActivity(
                    Intent(this, DiaryImageDetailActivity::class.java)
                        .putStringArrayListExtra(
                            "imgs",
                            viewModel.diary.value?.diaryImages?.map { it.imageUrl } as ArrayList<String>
                        )
                )
            }
        )

        binding.diaryGallerySavedRv.apply {
            adapter = galleryAdapter.apply {
                addItemDecoration(DiaryImageItemDecoration(this@PersonalDiaryDetailActivity, IMAGE_MARGIN))
                itemAnimator = null
            }
            layoutManager = LinearLayoutManager(this@PersonalDiaryDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun initObserve() {
        viewModel.diary.observe(this) { diary ->
            galleryAdapter.addImages(diary.diaryImages)
        }

        viewModel.addDiaryResult.observe(this) { response ->
            if (response.isSuccess) {
                Toast.makeText(this, "변경사항이 적용되었습니다", Toast.LENGTH_SHORT).show()
                viewModel.getDiaryData()
            } else {
                Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.editDiaryResult.observe(this) { response ->
            if (response.isSuccess) {
                Toast.makeText(this, "변경사항이 적용되었습니다", Toast.LENGTH_SHORT).show()
                viewModel.getDiaryData()
            } else {
                Toast.makeText(this, "e${response.message}rror", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteDiaryResult.observe(this) { response ->
            if (response.isSuccess) {
                Toast.makeText(this, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
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
            DELETE_BUTTON_ACTION -> viewModel.deleteDiary() // 삭제
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
        val currentImageCount = viewModel.diary.value?.diaryImages?.size ?: 0
        if (imageUris.isNullOrEmpty()) return@registerForActivityResult
        if (currentImageCount + imageUris.size > 3) {
            Toast.makeText(this, "사진은 총 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addImages(imageUris)
        }
    }

    private fun getImageUrisFromResult(result: ActivityResult): List<Uri> {
        if (result.resultCode != Activity.RESULT_OK) return emptyList()
        return result.data?.let { data ->
            mutableListOf<Uri>().apply {
                // 다중 선택 처리
                data.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) { // 최대 3개
                        add(clipData.getItemAt(i).uri)
                    }
                } ?: data.data?.let { uri ->
                    // 단일 선택 처리
                    add(uri)
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
