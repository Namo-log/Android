package com.mongmong.namo.presentation.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.diary.DiaryGroupSchedule
import com.mongmong.namo.data.remote.diary.*
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.databinding.ActivityDiaryGroupMemoBinding
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetGroupDiaryResponse
import com.mongmong.namo.domain.model.GroupDiaryResult
import com.mongmong.namo.domain.model.GroupUser
import com.mongmong.namo.domain.model.LocationDto
import com.mongmong.namo.presentation.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.mongmong.namo.presentation.ui.bottom.diary.groupDiary.adapter.GroupPlaceScheduleAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import kotlinx.coroutines.*
import org.joda.time.DateTime
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GroupMemoActivity : AppCompatActivity(), GetGroupDiaryView,
    ConfirmDialogInterface {  // 그룹 다이어리 추가, 수정, 삭제 화면

    private lateinit var binding: ActivityDiaryGroupMemoBinding

    private lateinit var memberadapter: GroupMemberRVAdapter  // 그룹 멤버 리스트 보여주기
    private lateinit var placeadapter: GroupPlaceScheduleAdapter // 각 장소 item

    private lateinit var groupMembers: List<GroupUser>
    private lateinit var groupData: GroupDiaryResult

    private lateinit var memberIntList: List<Long>
    private lateinit var repo: DiaryRepository
    private lateinit var moimSchedule: MoimSchedule

    private var groupSchedule = emptyList<LocationDto>()
    private var placeSchedule = ArrayList<DiaryGroupSchedule>()
    private var diaryService = DiaryService()

    private var imgList: ArrayList<String?> = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1
    private var groupScheduleId: Long = 0L

    private val itemTouchSimpleCallback = ItemTouchHelperCallback()  // 아이템 밀어서 삭제
    private val itemTouchHelper = ItemTouchHelper(itemTouchSimpleCallback)

    private var deleteItems = mutableListOf<Long>()
    private var deleteCount: Int = 0
    private var allDeleteFrag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryGroupMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = DiaryRepository(this)

        groupScheduleId = intent.getLongExtra("groupScheduleId", 0L)  // 그룹 스케줄 아이디
        val getHasDiaryBoolean = intent.getBooleanExtra("hasGroupPlace", false)

        hasDiaryPlace(getHasDiaryBoolean)
        onClickListener()

    }

    private fun hasDiaryPlace(getHasDiaryBoolean: Boolean) {
        if (!getHasDiaryBoolean) {  // groupPlace가 없을 때, 저장하기
            moimSchedule = intent?.getSerializableExtra("groupSchedule") as MoimSchedule
            initialize()
            bind()

            binding.groupSaveTv.text = resources.getString(R.string.diary_add)
            binding.groupSaveTv.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            binding.groupSaveTv.setBackgroundResource(R.color.MainOrange)
            binding.diaryDeleteIv.visibility = View.GONE


        } else { // groupPlace가 있을 때, 서버에서 데이터 가져오고 수정하기

            val diaryService = DiaryService()
            diaryService.getGroupDiary(groupScheduleId)
            diaryService.getGroupDiaryView(this)

            binding.groupSaveTv.text = resources.getString(R.string.diary_edit)
            binding.groupSaveTv.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.MainOrange
                )
            )
            binding.groupSaveTv.setBackgroundResource(R.color.white)
            binding.diaryDeleteIv.visibility = View.VISIBLE

            deletePlace()
        }

        addAndEditPlace()

    }


    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    override fun onGetGroupDiarySuccess(response: GetGroupDiaryResponse) {
        Log.d("GET_GROUP_DIARY", response.toString())

        val result = response.result
        groupMembers = result.users
        groupData = result
        groupSchedule = result.locationDtos

        memberIntList = groupMembers.map { it.userId }

        placeSchedule.addAll(groupSchedule.map {
            val copy = it.copy(
                imgs = it.imgs.toMutableList()
            )
            DiaryGroupSchedule(
                copy.place,
                copy.pay,
                copy.members,
                copy.imgs as ArrayList<String?>,
                copy.moimMemoLocationId
            )
        })

        val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(groupData.startDate * 1000)
        binding.groupAddInputDateTv.text = formatDate
        binding.groupAddInputPlaceTv.text = groupData.locationName
        binding.groupAddTitleTv.text = groupData.name

        onRecyclerView()
    }

    override fun onGetGroupDiaryFailure(message: String) {
        Log.e("GET_GROUP_DIARY", message)
    }

    private fun bind() {

        // 그룹 장소가 없을 때, 그룹 스케줄에서 가져온 데이터 바인딩
        val formatDate = DateTime(moimSchedule.startDate * 1000).toString("yyyy.MM.dd (EE)")

        binding.groupAddInputDateTv.text = formatDate
        binding.groupAddInputPlaceTv.text = moimSchedule.locationName
        binding.groupAddTitleTv.text = moimSchedule.name

        binding.groupSaveTv.text = "기록 저장"
        binding.groupSaveTv.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )
        binding.groupSaveTv.setBackgroundResource(R.color.MainOrange)

        val members = arrayListOf<GroupUser>()
        moimSchedule.users.map {
            members.add(GroupUser(it.userId, it.userName))
        }

        groupMembers = members
        memberIntList = groupMembers.map { it.userId }

        onRecyclerView()

    }

    private fun addAndEditPlace() {
        binding.groupSaveTv.setOnClickListener {
            val hasNullPlace = placeSchedule.any { it.place == "" }
            if (!hasNullPlace) {
                CoroutineScope(Dispatchers.Main).launch {
                    placeSchedule.forEach { diaryGroupSchedule ->
                        withContext(Dispatchers.IO) {
                            addOrEditGroupDiary(diaryGroupSchedule)
                        }
                    }
                    deleteItems.forEach { placeIdx ->
                        withContext(Dispatchers.IO) {
                            deleteGroupDiary(placeIdx)
                        }
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        applicationContext,
                        "장소를 입력해주세요!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        }
    }

    private fun addOrEditGroupDiary(diaryGroupSchedule: DiaryGroupSchedule) {
        val hasDiffer = groupSchedule.any { group ->
            group.place == diaryGroupSchedule.place &&
                    group.pay == diaryGroupSchedule.pay &&
                    group.members == diaryGroupSchedule.members &&
                    group.imgs == diaryGroupSchedule.imgs.filterNotNull()
        }
        val members = diaryGroupSchedule.members.ifEmpty { memberIntList }
        if (!hasDiffer) {
            if (diaryGroupSchedule.placeIdx == 0L) {
                repo.addMoimDiary(
                    groupScheduleId,
                    diaryGroupSchedule.place,
                    diaryGroupSchedule.pay,
                    members,
                    diaryGroupSchedule.imgs.filterNotNull(),
                    object : DiaryBasicView {
                        override fun onSuccess(response: DiaryResponse) {
                            Log.d("GROUP_DIARY_ADD", "SUCCESS")
                            finish()
                        }

                        override fun onFailure(message: String) {
                            Log.d("GROUP_DIARY_ADD", message)
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    applicationContext,
                                    "장소를 입력해주세요!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    })
            } else {
                repo.editGroupPlace(
                    diaryGroupSchedule.placeIdx,
                    diaryGroupSchedule.place,
                    diaryGroupSchedule.pay,
                    members,
                    diaryGroupSchedule.imgs.filterNotNull(),
                    object : DiaryBasicView {
                        override fun onSuccess(response: DiaryResponse) {
                            Log.d("GROUP_DIARY_EDIT", "SUCCESS")
                            finish()
                        }

                        override fun onFailure(message: String) {
                            Log.d("GROUP_DIARY_EDIT", message)
                            finish()
                        }
                    })
            }

        }
    }

    private fun deleteGroupDiary(placeIdx: Long) {
        try {
            diaryService.deleteGroupDiary(placeIdx,
                object : DiaryBasicView {
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onSuccess(response: DiaryResponse) {
                        Log.e("DELETE_GROUP_DIARY", "SUCCESS")

                        if (allDeleteFrag) deleteCount--
                        if (deleteCount == 0) {
                            finish()
                            allDeleteFrag = false
                        }
                    }

                    override fun onFailure(message: String) {
                        Log.e("DELETE_GROUP_DIARY", message)
                        finish()
                    }
                })
        } catch (e: Exception) {
            Log.e("DELETE_GROUP_DIARY", e.message ?: "알 수 없는 오류 발생")
            finish()
        }
    }


    private fun deletePlace() {  // 장소 전체 삭제 버튼
        binding.diaryDeleteIv.setOnClickListener {
            showDeleteDialog()
        }
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
                    deleteGroupDiary(it.placeIdx)
                }
            }
        }
    }

    private fun initialize() {
        with(placeSchedule) {
            add(DiaryGroupSchedule("", 0, arrayListOf(), arrayListOf()))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onRecyclerView() {

        binding.apply {

            // 멤버 이름 리사이클러뷰
            memberadapter = GroupMemberRVAdapter(groupMembers)
            groupAddPeopleRv.adapter = memberadapter
            groupAddPeopleRv.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)

            // 장소 추가 리사이클러뷰
            placeadapter = GroupPlaceScheduleAdapter(
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
                    this@GroupMemoActivity.imgList = imgs
                    this@GroupMemoActivity.positionForGallery = position

                    getPermission()
                },
                placeClickListener = { text, position ->
                    placeSchedule[position].place = text
                },
                deleteItemList = { deleteItem ->
                    deleteItems = deleteItem
                }
            )

            diaryGroupAddPlaceRv.adapter = placeadapter
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

    @SuppressLint("NotifyDataSetChanged")
    private fun onClickListener() {

        binding.upArrow.setOnClickListener {// 화살표가 위쪽 방향일 때 리사이클러뷰 숨쪽기
            setMember(true)
        }

        binding.bottomArrow.setOnClickListener {
            setMember(false)
        }

        binding.groupAddBackIv.setOnClickListener { // 뒤로가기
            finish()
        }

        //  장소 추가 버튼 클릭리스너
        binding.groudPlaceAddTv.setOnClickListener {

            if (placeSchedule.size >= 3) Toast.makeText(
                this,
                "장소 추가는 3개까지 가능합니다",
                Toast.LENGTH_SHORT
            )
                .show()
            else {
                placeSchedule.add(DiaryGroupSchedule("", 0, arrayListOf(), arrayListOf()))
                placeadapter.notifyDataSetChanged()
            }
        }

    }

    private fun setMember(isVisible: Boolean) {  // 그룹 멤버 리스트 세팅
        if (isVisible) {
            binding.groupAddPeopleRv.visibility = View.GONE
            binding.bottomArrow.visibility = View.VISIBLE
            binding.upArrow.visibility = View.GONE

        } else {
            binding.groupAddPeopleRv.visibility = View.VISIBLE
            binding.bottomArrow.visibility = View.GONE
            binding.upArrow.visibility = View.VISIBLE
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
            imgList.clear()
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
            }
        } else { // 단일 선택
            result.data?.data?.let {
                val imageUri: Uri? = result.data!!.data
                if (imageUri != null) {
                    imgList.add(imageUri.toString())
                }
            }
        }

        val position = this.positionForGallery
        val images = this.imgList


        if (position != RecyclerView.NO_POSITION) {
            placeSchedule[position].imgs = images
            placeadapter.addImageItem(images)
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