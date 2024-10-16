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
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.presentation.ui.diary.DiaryImageDetailActivity
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

    private var activityPosition: Int = 0

    override fun setup() {
        binding.apply {
            viewModel = this@MoimDiaryDetailActivity.viewModel
            // marquee focus
            moimDiaryTitleTv.requestFocus()
            moimDiaryTitleTv.isSelected = true
        }

        setupParticipants()
        setupViewPager()
        setupViewData()
        initClickListener()
        initObserve()
    }

    private fun setupViewData() {
        viewModel.getScheduleForDiary(intent.getLongExtra("scheduleId", 0))
        if (viewModel.diarySchedule.value?.hasDiary == false) {
            viewModel.setupNewDiary()
        } else {
            viewModel.getDiaryData()
        }
        viewModel.getActivitiesData()
        viewModel.getTotalMoimPayment()
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
                override fun onGuideClicked() {
                    Toast.makeText(this@MoimDiaryDetailActivity, "일기장은 본인만 확인할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
                override fun onEditModeClicked() { viewModel.setIsEditMode(true) }
                override fun onViewModeClicked() { showBackDialog(isModeChange = true) }

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
                override fun onDeleteDiary() {
                    showDeleteDialog(isActivity = false)
                }

            },
            activityEventListener = object : MoimDiaryVPAdapter.OnActivityEventListener {
                override fun onEditModeClicked() { viewModel.setIsEditMode(true) }
                override fun onViewModeClicked() { showBackDialog(isModeChange = true) }
                override fun onDeleteActivity(position: Int) {
                    activityPosition = position
                    showDeleteDialog(isActivity = true)
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

                override fun onAddImageClicked(position: Int) {
                    positionForGallery = position
                    getGallery()
                }
                override fun onImageClicked(images: List<DiaryImage>) {
                    startActivity(Intent(this@MoimDiaryDetailActivity, DiaryImageDetailActivity::class.java)
                        .putStringArrayListExtra("imgs", images.map { it.imageUrl } as ArrayList<String>))
                }

                override fun onDeleteImage(position: Int, image: DiaryImage) {
                    viewModel.deleteActivityImage(position, image)
                }
                override fun onParticipantsClicked(position: Int) { showActivityParticipantsDialog(position) }
                override fun onPayClicked(position: Int) { showActivityPaymentDialog(position) }
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

        viewModel.addDiaryResult.observe(this) { response ->
            if(response.isSuccess) {
                viewModel.getDiaryData()
                Toast.makeText(this, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.setIsEditMode(false)
            }
            else Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
            binding.moimDiarySaveBtn.isEnabled = true
        }

        viewModel.editDiaryResult.observe(this) { response ->
            if(response.isSuccess) {
                viewModel.getDiaryData()
                viewModel.getActivitiesData()
                viewModel.getTotalMoimPayment()
                Toast.makeText(this, "변경 사항이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.setIsEditMode(false)
            }
            else Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
            binding.moimDiarySaveBtn.isEnabled = true
        }

        viewModel.deleteDiaryResult.observe(this) { response ->
            if(response.isSuccess) {
                viewModel.setupNewDiary()
                Toast.makeText(this, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.setIsEditMode(false)
            }
            else Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
            binding.moimDiarySaveBtn.isEnabled = true
        }
    }

    private fun initClickListener() {
        // 뒤로가기
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.diaryChanged.value == true) {
                    showBackDialog(false)
                } else finish()
            }
        })
        binding.moimDiaryBackIv.setOnClickListener {
            if (viewModel.diaryChanged.value == true) {
                showBackDialog(false)
            } else finish()
        }

        // 전체 정산
        binding.moimPaymentTv.setOnClickListener {
            if(viewModel.moimPayment.value?.totalAmount != 0) showMoimPaymentDialog()
        }

        //  활동 추가 버튼 클릭리스너
        binding.moimActivityAddLy.setOnClickListener {
            if (viewModel.activities.value?.size ?: 0 >= 3)
                Toast.makeText(this, "활동 추가는 3개까지 가능합니다", Toast.LENGTH_SHORT).show()
            else { viewModel.addEmptyActivity() }
        }

        // 기록 추가 or 기록 수정
        binding.moimDiarySaveBtn.setOnClickListener {
            if (viewModel.diarySchedule.value?.hasDiary == false) {
                viewModel.addDiary()
            } else {
                Log.d("MoimDiaryDetailActivity", "editDiary")
                viewModel.editDiary()
            }
            binding.moimDiarySaveBtn.isEnabled = false
        }
    }

    private fun showDeleteDialog(isActivity: Boolean) {
        val title = if (isActivity) DELETE_ACTIVITY_TITLE else DELETE_DIARY_TITLE
        val content = if (isActivity) DELETE_ACTIVITY_CONTENT else DELETE_DIARY_CONTENT

        val dialog = ConfirmDialog(this, title, content, "확인",
            if (isActivity) DELETE_ACTIVITY_BUTTON_ACTION else DELETE_DIARY_BUTTON_ACTION
        )
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    private fun showBackDialog(isModeChange: Boolean) {
        val title = BACK_TITLE
        val content = if(isModeChange) VIEW_CONTENT else BACK_CONTENT

        val dialog = ConfirmDialog(
            this, title, content, "확인",
            if(isModeChange) VIEW_BUTTON_ACTION else BACK_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    override fun onClickYesButton(id: Int) {
        when(id) {
            DELETE_DIARY_BUTTON_ACTION -> viewModel.deleteDiary() // 일기 삭제
            DELETE_ACTIVITY_BUTTON_ACTION -> viewModel.deleteActivity(activityPosition) // 활동 삭제
            VIEW_BUTTON_ACTION -> {
                viewModel.getDiaryData()
                viewModel.getActivitiesData()
                viewModel.getTotalMoimPayment()
                viewModel.setIsEditMode(false)
            }
            BACK_BUTTON_ACTION -> finish() // 뒤로가기
        }
    }

    private fun showMoimPaymentDialog() {
        val dialog = MoimPaymentDialog()
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
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
        const val DELETE_DIARY_BUTTON_ACTION = 1
        const val DELETE_ACTIVITY_BUTTON_ACTION = 2
        const val BACK_BUTTON_ACTION = 3
        const val VIEW_BUTTON_ACTION = 4
        const val DELETE_DIARY_TITLE = "일기를 삭제하시겠어요?"
        const val DELETE_DIARY_CONTENT = "삭제한 일기는 내 기록에서 삭제됩니다."
        const val DELETE_ACTIVITY_TITLE = "활동을 삭제하시겠어요?"
        const val DELETE_ACTIVITY_CONTENT = "삭제한 모임 활동은\n모든 참석자의 기록에서 삭제됩니다."
        const val BACK_TITLE = "편집한 내용이 저장되지 않습니다."
        const val BACK_CONTENT = "정말 나가시겠어요?"
        const val VIEW_CONTENT = "정말 조회 모드로 돌아가시겠어요?"
    }
}

