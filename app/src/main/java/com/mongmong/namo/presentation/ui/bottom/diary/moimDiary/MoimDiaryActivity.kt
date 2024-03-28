package com.mongmong.namo.presentation.ui.bottom.diary.moimDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.data.remote.diary.*
import com.mongmong.namo.databinding.ActivityMoimDiaryBinding
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.domain.model.MoimDiaryResult
import com.mongmong.namo.domain.model.GroupUser
import com.mongmong.namo.domain.model.MoimActivity
import com.mongmong.namo.presentation.ui.bottom.diary.moimDiary.adapter.MoimMemberRVAdapter
import com.mongmong.namo.presentation.ui.bottom.diary.moimDiary.adapter.MoimActivityRVAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class MoimDiaryActivity : AppCompatActivity(),
    ConfirmDialogInterface {  // 그룹 다이어리 추가, 수정, 삭제 화면

    private lateinit var binding: ActivityMoimDiaryBinding

    private lateinit var memberadapter: MoimMemberRVAdapter  // 그룹 멤버 리스트 보여주기
    private lateinit var activityAdapter: MoimActivityRVAdapter // 각 장소 item

    private lateinit var groupMembers: List<GroupUser>
    private lateinit var groupData: MoimDiaryResult

    private lateinit var memberIntList: List<Long>
    private lateinit var repo: DiaryRepository
    private lateinit var moimSchedule: MoimSchedule

    private var preActivities = emptyList<MoimActivity>()
    private var placeSchedule = ArrayList<MoimActivity>()

    private var imgList: ArrayList<String>? = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1
    private var moimScheduleId: Long = 0L

    private val itemTouchSimpleCallback = ItemTouchHelperCallback()  // 아이템 밀어서 삭제
    private val itemTouchHelper = ItemTouchHelper(itemTouchSimpleCallback)

    private var deleteItems = mutableListOf<Long>()
    private var deleteCount: Int = 0
    private var allDeleteFrag = false

    private val viewModel : MoimDiaryViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoimDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("GROUP_DIARY_CLICK", "onCreate")

        repo = DiaryRepository(this)

        moimScheduleId = intent.getLongExtra("groupScheduleId", 0L)  // 그룹 스케줄 아이디
        val getHasDiaryBoolean = intent.getBooleanExtra("hasGroupPlace", false)

        hasDiaryPlace(getHasDiaryBoolean)
        onClickListener()

        initObserve()
    }

    private fun hasDiaryPlace(getHasDiaryBoolean: Boolean) {
        if (!getHasDiaryBoolean) {  // groupPlace가 없을 때, 저장하기
            moimSchedule = intent?.getSerializableExtra("groupSchedule") as MoimSchedule
            Log.d("hasDiaryPlace", "$moimSchedule")
            placeSchedule.add(MoimActivity(0L, "", 0, arrayListOf(), arrayListOf()))
            setViewOnNoDiary()
            binding.groupSaveTv.apply {
                text = resources.getString(R.string.diary_add)
                setTextColor(getColor(R.color.white))
                setBackgroundResource(R.color.MainOrange)
            }
            binding.diaryDeleteIv.visibility = View.GONE
        } else { // groupPlace가 있을 때, 서버에서 데이터 가져오고 수정하기
            viewModel.getMoimDiary(moimScheduleId)
            binding.groupSaveTv.apply {
                text = resources.getString(R.string.diary_edit)
                setTextColor(getColor(R.color.MainOrange))
                setBackgroundResource(R.color.white)
            }
            binding.diaryDeleteIv.visibility = View.VISIBLE
        }
    }

    private fun onClickListener() {
        // 참석자 숨기기 버튼
        binding.upArrow.setOnClickListener { setMember(true) }
        binding.bottomArrow.setOnClickListener { setMember(false) }
        // 뒤로가기
        binding.groupAddBackIv.setOnClickListener { finish() }

        //  장소 추가 버튼 클릭리스너
        binding.groudPlaceAddTv.setOnClickListener {
            if (placeSchedule.size >= 3)
                Toast.makeText(this, "장소 추가는 3개까지 가능합니다", Toast.LENGTH_SHORT).show()
            else {
                placeSchedule.add(MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf()))
                activityAdapter.notifyDataSetChanged()
            }
        }

        // 기록 추가 or 기록 수정
        binding.groupSaveTv.setOnClickListener {
            if (!placeSchedule.any { it.place == "" }) {
                viewModel.patchMoimActivities(
                    preActivities,
                    memberIntList,
                    moimScheduleId,
                    placeSchedule,
                    deleteItems
                )
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
        viewModel.getMoimDiaryResult.observe(this) { result ->
            groupMembers = result.users
            groupData = result
            preActivities = result.moimActivities

            memberIntList = groupMembers.map { it.userId }

            placeSchedule.addAll(preActivities.map {
                val copy = it.copy(
                    imgs = it.imgs?.toMutableList()
                )
                MoimActivity(
                    copy.moimActivityId,
                    copy.place,
                    copy.pay,
                    copy.members,
                    copy.imgs,
                )
            })

            val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(groupData.startDate * 1000)
            binding.groupAddInputDateTv.text = formatDate
            binding.groupAddInputPlaceTv.text = groupData.locationName
            binding.groupAddTitleTv.text = groupData.name

            onRecyclerView()
        }

        viewModel.patchActivitiesComplete.observe(this) { isComplete ->
            Log.d("MoimActivity", "finish")
            if(isComplete) finish()
            else Toast.makeText(this, "네트워크 오류", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViewOnNoDiary() {
        // 그룹 장소가 없을 때, 그룹 스케줄에서 가져온 데이터 바인딩
        val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(moimSchedule.startDate * 1000)
        binding.groupAddInputDateTv.text = formatDate
        binding.groupAddInputPlaceTv.text = moimSchedule.locationName
        binding.groupAddTitleTv.text = moimSchedule.name

        val members = arrayListOf<GroupUser>()
        moimSchedule.users.map {
            members.add(GroupUser(it.userId, it.userName))
        }

        groupMembers = members
        memberIntList = groupMembers.map { it.userId }

        onRecyclerView()

    }



    private fun showDeleteDialog() {
        // 삭제 확인 다이얼로그
        val title = "모임 기록을 정말 삭제하시겠어요?"
        val content = "삭제한 모든 모임 기록은\n개인 기록 페이지에서도 삭제됩니다."

        val dialog = ConfirmDialog(this, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    override fun onClickYesButton(id: Int) {

        allDeleteFrag = true
        deleteCount = placeSchedule.size

        // 모임 기록 전체 삭제
        CoroutineScope(Dispatchers.Main).launch {
            placeSchedule.forEach {
                withContext(Dispatchers.IO) {
                    //deleteGroupDiary(it.placeIdx)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onRecyclerView() {
        binding.apply {

            // 멤버 이름 리사이클러뷰
            memberadapter = MoimMemberRVAdapter(groupMembers)
            groupAddPeopleRv.adapter = memberadapter
            groupAddPeopleRv.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)

            // 장소 추가 리사이클러뷰
            activityAdapter = MoimActivityRVAdapter(
                applicationContext,
                placeSchedule,
                payClickListener = { _, position, payText ->
                    GroupPayDialog(groupMembers, placeSchedule[position], {
                        placeSchedule[position].pay = it
                        payText.text = NumberFormat.getNumberInstance(Locale.US).format(it)

                    }, {
                        placeSchedule[position].members = it
                    }).show(supportFragmentManager, "show")
                    binding.diaryGroupAddPlaceRv.smoothScrollToPosition(position)
                },
                imageClickListener = { imgs, position ->
                    this@MoimDiaryActivity.imgList = imgs as ArrayList<String>?
                    this@MoimDiaryActivity.positionForGallery = position

                    getPermission()
                },
                placeClickListener = { text, position ->
                    placeSchedule[position].place = text
                },
                deleteItemList = { deleteItem ->
                    deleteItems = deleteItem
                }
            )

            diaryGroupAddPlaceRv.adapter = activityAdapter
            diaryGroupAddPlaceRv.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

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
    }


    private fun setMember(isVisible: Boolean) {  // 그룹 멤버 리스트 세팅
        with(binding) {
            groupAddPeopleRv.visibility = if (isVisible) View.GONE else View.VISIBLE
            bottomArrow.visibility = if (isVisible) View.VISIBLE else View.GONE
            upArrow.visibility = if (isVisible) View.GONE else View.VISIBLE
        }
    }

    @SuppressLint("IntentReset")
    private fun getPermission() { // 갤러리 권한 여부 확인, 권한이 있을 때만 이미지 가져오기

        val writePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                200
            )
        } else {
            // 권한 있음
            val intent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }

            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기

            getImage.launch(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imgList?.clear()
            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(applicationContext, "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri
                        imgList?.add(imageUri.toString())

                    }
                }
            }
        } else { // 단일 선택
            result.data?.data?.let {
                val imageUri: Uri? = result.data!!.data
                if (imageUri != null) {
                    imgList?.add(imageUri.toString())
                }
            }
        }

        val position = this.positionForGallery
        val images = this.imgList


        if (position != RecyclerView.NO_POSITION) {
            placeSchedule[position].imgs = images
            activityAdapter.addImageItem(images)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {  // editText 외 터치 시 키보드 내려감
        val focusView = currentFocus
        if (focusView != null && ev != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()

            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}