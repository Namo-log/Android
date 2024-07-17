package com.mongmong.namo.presentation.ui.group.diary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.databinding.ActivityMoimDiaryBinding
import com.mongmong.namo.domain.model.group.MoimActivity
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.presentation.ui.diary.DiaryImageDetailActivity
import com.mongmong.namo.presentation.ui.diary.PersonalDetailActivity
import com.mongmong.namo.presentation.ui.group.diary.adapter.MoimActivityItemDecoration
import com.mongmong.namo.presentation.ui.group.diary.adapter.MoimActivityRVAdapter
import com.mongmong.namo.presentation.ui.group.diary.adapter.MoimMemberRVAdapter
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.dpToPx
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.PermissionChecker
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*


@AndroidEntryPoint
class MoimDiaryActivity : AppCompatActivity(), ConfirmDialogInterface {  // 그룹 다이어리 추가, 수정, 삭제 화면

    private lateinit var binding: ActivityMoimDiaryBinding

    private lateinit var memberAdapter: MoimMemberRVAdapter  // 그룹 멤버 리스트 보여주기
    private lateinit var activityAdapter: MoimActivityRVAdapter // 각 장소 item

    private var positionForGallery: Int = -1
    private val viewModel: MoimDiaryViewModel by viewModels()

    private val itemTouchSimpleCallback = ItemTouchHelperCallback()  // 아이템 밀어서 삭제
    private val itemTouchHelper = ItemTouchHelper(itemTouchSimpleCallback)

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedImgs = result.data?.getStringArrayListExtra("imgs")
            updatedImgs?.let {
                viewModel.updateActivityImages(positionForGallery, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoimDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewModel = this@MoimDiaryActivity.viewModel
            lifecycleOwner = this@MoimDiaryActivity
        }

        viewModel.moimScheduleId = intent.getLongExtra("moimScheduleId", 0L)

        initView()
        onClickListener()
        initObserve()
    }

    private fun initView() {
        viewModel.isEdit.value = intent.getBooleanExtra("hasMoimActivity", false)
        if (viewModel.isEdit.value == false) {
            viewModel.setNewMoimDiary(intent?.getSerializableExtra("moimSchedule") as MoimScheduleBody)
        } else {
            viewModel.getMoimDiary(viewModel.moimScheduleId)
        }
        setRecyclerView()
    }

    private fun onClickListener() {
        // 뒤로가기
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isDiaryChanged()) {
                    showBackDialog()
                } else finish()
            }
        })
        binding.groupAddBackIv.setOnClickListener {
            if (viewModel.isDiaryChanged()) {
                showBackDialog()
            } else finish()
        }

        //  장소 추가 버튼 클릭리스너
        binding.groudPlaceAddTv.setOnClickListener {
            if (viewModel.activities.value?.size ?: 0 >= 3)
                Toast.makeText(this, "장소 추가는 3개까지 가능합니다", Toast.LENGTH_SHORT).show()
            else {
                viewModel.addActivity(MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf()))
            }
        }

        // 기록 추가 or 기록 수정
        binding.groupSaveTv.setOnClickListener {
            if (viewModel.activities.value?.any { it.name.isEmpty() } == false) {
                viewModel.patchMoimActivities()
            } else {
                Toast.makeText(this@MoimDiaryActivity, "장소를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        // 기록 삭제
        binding.diaryDeleteIv.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun initObserve() {
        viewModel.moimDiary.observe(this) { result ->
            // 초기화된 어댑터에 데이터 전달
            activityAdapter.submitList(result.moimActivities)
            memberAdapter.submitList(result.users)
        }

        viewModel.activities.observe(this) { activities ->
            activityAdapter.submitList(activities)
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

    private fun setRecyclerView() {
        // 멤버 이름 리사이클러뷰
        binding.moimParticipantRv.apply {
            memberAdapter = MoimMemberRVAdapter()
            adapter = memberAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        }
        // 장소 추가 리사이클러뷰
        activityAdapter = MoimActivityRVAdapter(
            payClickListener = ::onPayClickListener,
            imageDetailClickListener = { position ->
                positionForGallery = position
                startForResult.launch(
                    Intent(this, DiaryImageDetailActivity::class.java).apply {
                        putStringArrayListExtra("imgs", viewModel.activities.value?.get(position)?.imgs as ArrayList<String>?)
                    }
                )
            },
            updateImageClickListener = { position ->
                positionForGallery = position
                getGallery()
            },
            activityNameTextWatcher = { text, position ->
                viewModel.updateActivityName(position, text)
            },
            deleteItemList = { deleteItems ->
                viewModel.updateDeleteItems(deleteItems)
            },
            deleteImageClickListener = { position, image ->
                viewModel.deleteActivityImage(position, image)
            }
        )

        binding.diaryGroupAddPlaceRv.apply {
            adapter = activityAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(MoimActivityItemDecoration(dpToPx(context, 25f)))
        }
        itemTouchHelper.attachToRecyclerView(binding.diaryGroupAddPlaceRv)

        // RecyclerView의 다른 곳을 터치하거나 Swipe 시 기존에 Swipe된 것은 제자리로 변경
        binding.root.setOnTouchListener { _, _ ->
            itemTouchSimpleCallback.resetPreviousClamp(binding.diaryGroupAddPlaceRv)
            false
        }
        binding.scrollViewLayout.setOnTouchListener { _, _ ->
            itemTouchSimpleCallback.resetPreviousClamp(binding.diaryGroupAddPlaceRv)
            false
        }
        binding.diaryGroupAddPlaceRv.setOnTouchListener { _, _ ->
            itemTouchSimpleCallback.removePreviousClamp(binding.diaryGroupAddPlaceRv)
            false
        }

    }

    private fun onPayClickListener(pay: Long, position: Int, payText: TextView) {
        GroupPayDialog(
            viewModel.moimDiary.value?.users ?: emptyList(),
            viewModel.activities.value?.get(position)!!,
            { updatedPay ->
                viewModel.updateActivityPay(position, updatedPay)
                payText.text = NumberFormat.getNumberInstance(Locale.US).format(updatedPay)
            },
            { updatedMembers ->
                viewModel.updateActivityMembers(position, updatedMembers)
            }
        ).show(supportFragmentManager, "show")
        binding.diaryGroupAddPlaceRv.smoothScrollToPosition(position)
    }

    private fun showDeleteDialog() {
        // 삭제 확인 다이얼로그
        val title = "모임 기록을 정말 삭제하시겠어요?"
        val content = "삭제한 모든 모임 기록은\n개인 기록 페이지에서도 삭제됩니다."
        val dialog = ConfirmDialog(this, title, content, "삭제", DELETE_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    /** 뒤로가기 확인 다이얼로그 */
    private fun showBackDialog() {
        val title = "편집한 내용이 저장되지 않습니다."
        val content = "정말 나가시겠어요?"

        val dialog = ConfirmDialog(this, title, content, "확인",
            PersonalDetailActivity.BACK_BUTTON_ACTION
        )
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    override fun onClickYesButton(id: Int) {
        when(id) {
            PersonalDetailActivity.DELETE_BUTTON_ACTION -> viewModel.deleteMoimDiary() // 삭제
            PersonalDetailActivity.BACK_BUTTON_ACTION -> finish() // 뒤로가기
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

    @SuppressLint("NotifyDataSetChanged")
    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imgList = arrayListOf<String>()
            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(applicationContext, "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri
                        imgList.add(imageUri.toString())
                    }
                }
            } else { // 단일 선택
                result.data?.data?.let {
                    val imageUri: Uri? = result.data!!.data
                    if (imageUri != null) {
                        imgList.add(imageUri.toString())
                    }
                }
            }
            viewModel.updateActivityImages(positionForGallery, imgList)
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

