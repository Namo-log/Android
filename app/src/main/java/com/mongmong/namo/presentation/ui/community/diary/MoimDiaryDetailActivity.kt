package com.mongmong.namo.presentation.ui.community.diary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMoimDiaryDetailBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.presentation.ui.diary.DiaryImageDetailActivity
import com.mongmong.namo.presentation.ui.diary.PersonalDiaryDetailActivity
import com.mongmong.namo.presentation.ui.community.diary.adapter.MoimDiaryVPAdapter
import com.mongmong.namo.presentation.ui.community.diary.adapter.MoimDiaryParticipantsRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity.Companion.PLACE_ID_KEY
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity.Companion.PLACE_NAME_KEY
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity.Companion.PLACE_X_KEY
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity.Companion.PLACE_Y_KEY
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.PermissionChecker
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import java.lang.NullPointerException
import java.util.ArrayList


@AndroidEntryPoint
class MoimDiaryDetailActivity :
    BaseActivity<ActivityMoimDiaryDetailBinding>(R.layout.activity_moim_diary_detail),
    ConfirmDialogInterface {  // 그룹 다이어리 추가, 수정, 삭제 화면
    private lateinit var participantsAdapter: MoimDiaryParticipantsRVAdapter  // 일정 참여자
    private lateinit var vpAdapter: MoimDiaryVPAdapter // 일기장 + 활동

    private var positionForGallery = DIARY_POSITION
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
            viewModel.setupNewDiary()
        } else {  // 기록 있을 때, 수정
            viewModel.getDiaryData()
        }
    }

    private fun setupParticipants() {
        binding.moimParticipantRv.apply {
            participantsAdapter = MoimDiaryParticipantsRVAdapter()
            adapter = participantsAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupViewPager() {
        vpAdapter = MoimDiaryVPAdapter(
            diaryEventListener = object : MoimDiaryVPAdapter.OnDiaryEventListener{
                override fun onAddImageClicked() {
                    positionForGallery = DIARY_POSITION
                    getGallery()
                }
                override fun onImageClicked(images: List<DiaryImage>) {
                    startActivity(
                        Intent(this@MoimDiaryDetailActivity, DiaryImageDetailActivity::class.java)
                            .putStringArrayListExtra("imgs", images.map { it.imageUrl } as ArrayList<String>)
                    )
                }
                override fun onContentChanged(content: String) { viewModel.updateContent(content) }
                override fun onEnjoyClicked(enjoyRating: Int) { viewModel.updateEnjoy(enjoyRating) }
                override fun onDeleteImage(image: DiaryImage) { viewModel.deleteDiaryImage(image) }
                override fun onEditModeClicked() { viewModel.setIsEditMode(true) }
                override fun onDeleteDiary() {

                }
            },
            activityEventListener = object : MoimDiaryVPAdapter.OnActivityEventListener {
                override fun onAddImageClicked(position: Int) {
                    positionForGallery = position
                    getGallery()
                }
                override fun onDeleteActivity(position: Int) {
                    viewModel.deleteActivity(position)
                }
                override fun onActivityNameChanged(name: String, position: Int) {
                    viewModel.updateActivityName(position, name)
                }
                override fun onStartDateSelected(position: Int, date: String) {
                    viewModel.updateActivityStartDate(position, date)
                }
                override fun onEndDateSelected(position: Int, date: String) {
                    viewModel.updateActivityEndDate(position, date)
                }
                override fun onLocationClicked(position: Int) { getLocationPermission(position) }

                override fun onParticipantsClicked(position: Int) { showActivityParticipantsDialog(position) }
                override fun onPayClicked(position: Int) { showActivityPaymentDialog(position) }
                override fun onDeleteImage(image: DiaryImage) {
                }

                override fun onEditModeClicked() { viewModel.setIsEditMode(true) }
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
        viewModel.diarySchedule.observe(this) { diarySchedule ->
            participantsAdapter.submitList(diarySchedule.participantInfo)
            vpAdapter.setHasDiary(diarySchedule.hasDiary)
        }

        viewModel.diary.observe(this) { diary ->
            vpAdapter.updateDiary(diary)
        }

        viewModel.activities.observe(this) { activities ->
            vpAdapter.submitActivities(activities)
            binding.moimDiaryIndicator.setViewPager(binding.moimDiaryVp)
        }

        viewModel.isEditMode.observe(this) { isEditMode ->
            vpAdapter.setEditMode(isEditMode)
        }
        // 활동이 추가되면 마지막 페이지로 이동
        viewModel.isActivityAdded.observe(this) { isAdded ->
            if (isAdded) {
                val lastPage = vpAdapter.itemCount - 1
                binding.moimDiaryVp.setCurrentItem(lastPage, true)  // 마지막 페이지로 이동

                // 이벤트 처리 후 플래그 초기화
                viewModel.activityAddedHandled()
            }
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
            else { viewModel.addEmptyActivity() }
        }

        // 기록 추가 or 기록 수정
        binding.moimDiarySaveBtn.setOnClickListener {
            /*if (viewModel.activities.value?.any { it.name.isEmpty() } == false) {
                viewModel.patchMoimActivities()
            } else {
                Toast.makeText(this@MoimDiaryDetailActivity, "장소를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }*/
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

    private fun showActivityParticipantsDialog(position: Int) {
        val dialog = ActivityParticipantsDialog(position)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    private fun showActivityPaymentDialog(position: Int) {
        val dialog = ActivityPaymentDialog(position)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    private fun getLocationPermission(position: Int) {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(ORIGIN_ACTIVITY_INTENT_KEY, "MoimDiary")
                val placeData = viewModel.activities.value?.get(position)?.location

                if (placeData != null && placeData.locationName.isNotEmpty()) {
                    intent.apply {
                        putExtra(PLACE_NAME_KEY, placeData.locationName)
                        putExtra(PLACE_X_KEY, placeData.longitude)
                        putExtra(PLACE_Y_KEY, placeData.latitude)
                    }
                }
                getLocationResult.launch(intent)
            } catch (e : NullPointerException) {
                Log.e("LOCATION_ERROR", e.toString())
            }
        }
    }

    private val getLocationResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val placeId = result.data?.getStringExtra(PLACE_ID_KEY) ?: ""
            val placeName = result.data?.getStringExtra(PLACE_NAME_KEY) ?: ""
            val placeX = result.data?.getDoubleExtra(PLACE_X_KEY, 0.0)
            val placeY = result.data?.getDoubleExtra(PLACE_Y_KEY, 0.0)

            if (placeName != null && placeX != null && placeY != null) {
                viewModel.updateActivityLocation(binding.moimDiaryVp.currentItem - 1, placeId, placeName, placeX, placeY)
            }
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
            val newImages = mutableListOf<Uri>()
            result.data?.let { data ->
                if (data.clipData != null) { // 여러 개의 이미지를 선택한 경우
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        newImages.add(imageUri)
                    }
                } else { // 단일 이미지 선택
                    val imageUri: Uri? = data.data
                    imageUri?.let { newImages.add(it) }
                }
            }


            if (positionForGallery == DIARY_POSITION) {
                // 다이어리의 이미지 추가 처리
                val currentImagesCount = viewModel.diary.value?.diaryImages?.size ?: 0
                if (currentImagesCount + newImages.size > 3) {
                    Toast.makeText(this, "이미지는 최대 3개까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addDiaryImages(newImages)
                }
            } else {
                // 활동의 이미지 추가 처리
                val currentImagesCount = viewModel.activities.value?.get(positionForGallery)?.images?.size ?: 0
                if (currentImagesCount + newImages.size > 3) {
                    Toast.makeText(this, "이미지는 최대 3개까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addActivityImages(positionForGallery, newImages)
                }
            }
        }
    }



    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val DIARY_POSITION = -1
        const val DELETE_BUTTON_ACTION = 1
        const val BACK_BUTTON_ACTION = 2
    }
}

