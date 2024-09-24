package com.mongmong.namo.presentation.ui.group.diary

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMoimDiaryDetailBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.group.MoimActivity
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.presentation.ui.diary.DiaryImageDetailActivity
import com.mongmong.namo.presentation.ui.diary.PersonalDiaryDetailActivity
import com.mongmong.namo.presentation.ui.group.diary.adapter.MoimDiaryVPAdapter
import com.mongmong.namo.presentation.ui.group.diary.adapter.MoimParticipantsRVAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.PermissionChecker
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList


@AndroidEntryPoint
class MoimDiaryDetailActivity :
    BaseActivity<ActivityMoimDiaryDetailBinding>(R.layout.activity_moim_diary_detail),
    ConfirmDialogInterface {  // 그룹 다이어리 추가, 수정, 삭제 화면
    private lateinit var participantsAdapter: MoimParticipantsRVAdapter  // 일정 참여자
    private lateinit var vpAdapter: MoimDiaryVPAdapter // 각 활동 item

    private var positionForGallery: Int = -1
    private val viewModel: MoimDiaryViewModel by viewModels()

    override fun setup() {
        binding.apply {
            viewModel = this@MoimDiaryDetailActivity.viewModel
            // marquee focus
            moimDiaryTitleTv.requestFocus()
            moimDiaryTitleTv.isSelected = true
        }

        setupParticipants()
        setupViewPager()
        setupScheduleData()
        initClickListener()
        initObserve()
    }

    private fun setupScheduleData() {
        viewModel.getScheduleForDiary(intent.getLongExtra("scheduleId", 0))
        setCreateOrEdit()
    }

    private fun setCreateOrEdit() {
        if (viewModel.diarySchedule.value?.hasDiary == false) {
            Log.d("setCreateOrEdit", "dd")
            viewModel.setNewDiary()
        } else {  // 기록 있을 때, 수정
            Log.d("setCreateOrEdit", "dd2")
            viewModel.getDiary()
        }
    }


    private fun setupParticipants() {
        binding.moimParticipantRv.apply {
            participantsAdapter = MoimParticipantsRVAdapter()
            adapter = participantsAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupViewPager() {
        vpAdapter = MoimDiaryVPAdapter(
            diaryEventListener = object : MoimDiaryVPAdapter.OnDiaryEventListener{
                override fun onImageClicked(images: List<DiaryImage>) {
                    startActivity(
                        Intent(this@MoimDiaryDetailActivity, DiaryImageDetailActivity::class.java)
                            .putStringArrayListExtra("imgs", images.map { it.imageUrl } as ArrayList<String>)
                    )
                }
                override fun onContentChanged(content: String) { viewModel.updateContent(content) }
                override fun onEnjoyClicked(enjoyRating: Int) { viewModel.updateEnjoy(enjoyRating) }
                override fun onDeleteImage(image: DiaryImage) { viewModel.deleteDiaryImage(image) }

            },
            activityEventListener = object : MoimDiaryVPAdapter.OnActivityEventListener {
                override fun onDeleteActivity() {
                    //viewModel.deleteActivity()
                }

                override fun onUpdateImage(position: Int) {
                    // 이미지 업데이트 처리
                }

                override fun onActivityNameChanged(name: String, position: Int) {
                    viewModel.updateActivityName(position, name)
                }

                // 기타 콜백 구현
            }
        )

        // ViewPager2에 어댑터 연결
        binding.moimDiaryVp.apply {
            adapter = vpAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        binding.moimDiaryIndicator.setViewPager(binding.moimDiaryVp)
    }

    private fun initObserve() {
        viewModel.diary.observe(this) { diary ->
            vpAdapter.updateDiary(diary)
        }

        viewModel.activities.observe(this) { activities ->
            vpAdapter.submitActivities(activities)
        }

        viewModel.patchActivitiesComplete.observe(this) { isComplete ->
            if (isComplete) finish()
            else Toast.makeText(this, "네트워크 오류", Toast.LENGTH_SHORT).show()
        }

        viewModel.deleteDiaryComplete.observe(this) { isComplete ->
            if (isComplete) {
                if (intent.getStringExtra("from") == "moimMemo") {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                } else finish()
            } else Toast.makeText(this, "네트워크 오류", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initClickListener() {
        // 뒤로가기
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.diaryChanged.value == true) {
                    showBackDialog()
                } else finish()
            }
        })
        binding.moimDiaryBackIv.setOnClickListener {
            if (viewModel.diaryChanged.value == true) {
                showBackDialog()
            } else finish()
        }

        //  활동 추가 버튼 클릭리스너
        binding.moimActivityAddLy.setOnClickListener {
            if (viewModel.activities.value?.size ?: 0 >= 3)
                Toast.makeText(this, "활동 추가는 3개까지 가능합니다", Toast.LENGTH_SHORT).show()
            else {
                viewModel.addActivity(MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf()))
            }
        }

        // 기록 추가 or 기록 수정
        binding.moimDiarySaveBtn.setOnClickListener {
            if (viewModel.activities.value?.any { it.name.isEmpty() } == false) {
                viewModel.patchMoimActivities()
            } else {
                Toast.makeText(this@MoimDiaryDetailActivity, "장소를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        // 기록 삭제
        binding.moimDiaryDeleteIv.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        // 삭제 확인 다이얼로그
        val title = "모임 기록을 정말 삭제하시겠어요?"
        val content = "삭제한 모든 모임 기록은\n개인 기록 페이지에서도 삭제됩니다."
        val dialog = ConfirmDialog(this, title, content, "삭제", DELETE_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    private fun showBackDialog() {
        val title = "편집한 내용이 저장되지 않습니다."
        val content = "정말 나가시겠어요?"

        val dialog = ConfirmDialog(this, title, content, "확인", BACK_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    override fun onClickYesButton(id: Int) {
        when(id) {
            PersonalDiaryDetailActivity.DELETE_BUTTON_ACTION -> viewModel.deleteDiary() // 삭제
            PersonalDiaryDetailActivity.BACK_BUTTON_ACTION -> finish() // 뒤로가기
        }
    }

    private fun getGallery() {
        if (PermissionChecker.hasImagePermission(this)) {
            val galleryIntent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                type = "image/*"
                data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   // 다중 이미지 가져오기
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
        if (result.resultCode == Activity.RESULT_OK) {
            val newImages = mutableListOf<String>()
            result.data?.let { data ->
                if (data.clipData != null) { // 여러 개의 이미지를 선택한 경우
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        newImages.add(imageUri.toString())
                    }
                } else { // 단일 이미지 선택
                    val imageUri: Uri? = data.data
                    imageUri?.let { newImages.add(it.toString()) }
                }
            }

            val currentImagesCount = viewModel.activities.value?.get(positionForGallery)?.images?.size ?: 0
            if (currentImagesCount + newImages.size > 3) {
                Toast.makeText(this, "이미지는 최대 3개까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updateActivityImages(positionForGallery, newImages.map { DiaryImage(diaryImageId = 0, imageUrl = it, orderNumber = 0) })
            }
        }
    }



    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val DELETE_BUTTON_ACTION = 1
        const val BACK_BUTTON_ACTION = 2
    }
}

