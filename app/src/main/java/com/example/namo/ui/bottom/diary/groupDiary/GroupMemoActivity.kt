package com.example.namo.ui.bottom.diary.groupDiary

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.data.remote.diary.DiaryService
import com.example.namo.data.remote.diary.GetGroupDiaryView
import com.example.namo.data.remote.moim.MoimSchedule
import com.example.namo.databinding.ActivityDiaryGroupMemoBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPlaceEventAdapter
import java.text.SimpleDateFormat

class GroupMemoActivity : AppCompatActivity(), GetGroupDiaryView {  // 그룹 다이어리 추가, 수정, 삭제 화면

    private lateinit var binding: ActivityDiaryGroupMemoBinding

    private lateinit var memberadapter: GroupMemberRVAdapter
    private lateinit var placeadapter: GroupPlaceEventAdapter

    private lateinit var groupMembers: List<DiaryResponse.GroupUser>
    private lateinit var groupData: DiaryResponse.GroupDiaryResult

    private lateinit var memberIntList: List<Long>
    private lateinit var repo: DiaryRepository
    private lateinit var moimSchedule: MoimSchedule

    private var groupEvent = listOf<DiaryResponse.LocationDto>()
    private var placeEvent = ArrayList<DiaryGroupEvent>()

    private var imgList: ArrayList<String?> = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1

    private val itemTouchSimpleCallback = ItemTouchHelperCallback()
    private val itemTouchHelper = ItemTouchHelper(itemTouchSimpleCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryGroupMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = DiaryRepository(this)

        moimSchedule = intent.getSerializableExtra("groupEvent") as MoimSchedule

        val diaryService = DiaryService()
        diaryService.getGroupDiary(moimSchedule.moimScheduleId)
        diaryService.getGroupDiaryView(this)
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    override fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse) {
        Log.d("GET_GROUP_DIARY", response.toString())

        val result = response.result
        groupMembers = result.users
        groupData = result
        groupEvent = result.locationDtos

        memberIntList = groupMembers.map { it.userId }

        placeEvent.clear()
        groupEvent.forEach {
            placeEvent.add(
                DiaryGroupEvent(
                    it.place,
                    it.pay,
                    it.members,
                    it.imgs as ArrayList<String?>,
                    it.moimMemoLocationId
                )
            )
        }

        val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(groupData.startDate * 1000)
        binding.groupAddInputDateTv.text = formatDate
        binding.groupAddInputPlaceTv.text = groupData.locationName
        binding.groupAddTitleTv.text = "title"

        onRecyclerView()
        onClickListener()

        if (placeEvent.size == 0) {
            binding.groupSaveTv.text = "기록 저장"
            binding.groupSaveTv.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            binding.groupSaveTv.setBackgroundResource(R.color.MainOrange)

            addPlace()

        } else {
            binding.groupSaveTv.text = "기록 수정"
            binding.groupSaveTv.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.MainOrange
                )
            )
            binding.groupSaveTv.setBackgroundResource(R.color.white)

            editPlace()
        }
    }

    override fun onGetGroupDiaryFailure(message: String) {
        Log.d("GET_GROUP_DIARY", message)

        val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(moimSchedule.startDate * 1000)
        binding.groupAddInputDateTv.text = formatDate
        binding.groupAddInputPlaceTv.text = "장소 없음"
        binding.groupAddTitleTv.text = moimSchedule.name

        binding.groupSaveTv.text = "기록 저장"
        binding.groupSaveTv.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )
        binding.groupSaveTv.setBackgroundResource(R.color.MainOrange)

        val members= arrayListOf<DiaryResponse.GroupUser>()
        moimSchedule.users.map {
            members.add(DiaryResponse.GroupUser(it.userId,it.userName))
        }

        groupMembers=members
        memberIntList = groupMembers.map { it.userId }

        onRecyclerView()
        onClickListener()

        addPlace()
    }


    private fun addPlace() {

        initialize()

        binding.groupSaveTv.setOnClickListener {// 저장하기

            placeEvent.map {
                repo.addMoimDiary(
                    moimSchedule.moimScheduleId,
                    it.place,
                    it.pay,
                    it.members,
                    it.imgs as List<String>?
                )
            }
            finish()
        }

    }

    private fun editPlace() {

        binding.groupSaveTv.setOnClickListener {

            placeEvent.forEach {
                if (it.placeIdx == 0L) {
                    repo.addMoimDiary(
                        moimSchedule.moimScheduleId,
                        it.place,
                        it.pay,
                        it.members,
                        it.imgs as List<String>?
                    )

                } else {
                    // 바뀐 데이터가 있을 때만 변경하기

                    val hasDiffer = groupEvent.all { group ->
                        group.place == it.place && group.pay == it.pay && group.imgs == it.imgs
                    }

                    if (!hasDiffer) repo.editGroupPlace(
                        it.placeIdx,
                        it.place,
                        it.pay,
                        it.members,
                        it.imgs as List<String>?
                    )
                }
            }
            finish()
        }

    }

    private fun initialize() {
        with(placeEvent) {
            add(DiaryGroupEvent("", 0, arrayListOf(), arrayListOf()))
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
            placeadapter = GroupPlaceEventAdapter(
                applicationContext,
                placeEvent,
                payClickListener = { _, position, payText ->
                    GroupPayDialog(groupMembers, placeEvent[position], {
                        placeEvent[position].pay = it
                        payText.text = it.toString()

                    }, {
                        placeEvent[position].members = it
                    }).show(supportFragmentManager, "show")

                },
                imageClickListener = { imgs, position ->
                    this@GroupMemoActivity.imgList = imgs
                    this@GroupMemoActivity.positionForGallery = position

                    getPermission()

                },
                placeClickListener = { text, position ->
                    placeEvent[position].place = text
                })

            diaryGroupAddPlaceRv.adapter = placeadapter
            diaryGroupAddPlaceRv.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

            itemTouchHelper.attachToRecyclerView(binding.diaryGroupAddPlaceRv)

            // RecyclerView의 다른 곳을 터치하거나 Swipe 시 기존에 Swipe된 것은 제자리로 변경
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
            placeEvent.add(DiaryGroupEvent("", 0, arrayListOf(), arrayListOf()))
            placeadapter.notifyDataSetChanged()
        }

    }

    private fun setMember(isVisible: Boolean) {
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
    private fun getPermission() {

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


        if (this.positionForGallery != RecyclerView.NO_POSITION) {
            placeEvent[position].imgs = imgList

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