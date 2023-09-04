package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
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
import com.example.namo.databinding.ActivityDiaryGroupModifyBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPlaceEventAdapter
import java.text.SimpleDateFormat

class GroupModifyActivity : AppCompatActivity(), GetGroupDiaryView {  // 그룹 다이어리 추가 화면

    private lateinit var binding: ActivityDiaryGroupModifyBinding

    private lateinit var memberadapter: GroupMemberRVAdapter
    private lateinit var placeadapter: GroupPlaceEventAdapter

    private lateinit var groupMembers: List<DiaryResponse.GroupUser>
    private lateinit var groupData: DiaryResponse.GroupDiaryResult
    private lateinit var groupEvent: List<DiaryResponse.LocationDto>
    private lateinit var memberIntList: List<Long>
    private lateinit var repo: DiaryRepository
    private lateinit var moimSchedule: MoimSchedule

    private var imgList: ArrayList<String?> = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1

    private var placeEvent = ArrayList<DiaryGroupEvent>()
    private var getComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryGroupModifyBinding.inflate(layoutInflater)
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

        getComplete = true
        val result = response.result
        groupMembers = result.users
        groupData = result
        groupEvent = result.locationDtos

        memberIntList = groupMembers.map { it.userId }

        groupEvent.map {
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
        binding.groupAddTitleTv.text = groupData.locationName

        Log.d("placeEvent", placeEvent.toString())
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

        onRecyclerView()

        onClickListener()
    }

    override fun onGetGroupDiaryFailure(message: String) {
        Log.d("GET_GROUP_DIARY", message)
    }

    @SuppressLint("NotifyDataSetChanged")
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

    @SuppressLint()
    private fun editPlace() {

        binding.groupSaveTv.setOnClickListener {
            placeEvent.map {
                if (it.placeIdx == 0L) {
                    repo.addMoimDiary(
                        moimSchedule.moimScheduleId,
                        it.place,
                        it.pay,
                        it.members,
                        it.imgs as List<String>?
                    )

                } else {
                    repo.editGroupPlace(
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

    private fun onRecyclerView() {

        binding.apply {

            // 멤버 이름 리사이클러뷰
            memberadapter = GroupMemberRVAdapter(groupMembers)
            groupAddPeopleRv.adapter = memberadapter
            groupAddPeopleRv.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)

            // 장소 추가 리사이클러뷰
            placeadapter = GroupPlaceEventAdapter(applicationContext, placeEvent)
            diaryGroupAddPlaceRv.adapter = placeadapter
            diaryGroupAddPlaceRv.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

            val itemTouchHelperCallback = ItemTouchHelperCallback(placeadapter)
            val helper = ItemTouchHelper(itemTouchHelperCallback)
            // RecyclerView에 ItemTouchHelper 연결
            helper.attachToRecyclerView(binding.diaryGroupAddPlaceRv)

            // 정산 다이얼로그
            placeadapter.groupPayClickListener(object : GroupPlaceEventAdapter.PayInterface {
                override fun onPayClicked(
                    pay: Long,
                    position: Int,
                    payText: TextView
                ) {
                    GroupPayDialog(groupMembers, placeEvent[position], {
                        placeEvent[position].pay = it
                        payText.text = it.toString()

                    }, {
                        placeEvent[position].members = it
                    }).show(supportFragmentManager, "show")
                }
            })


            // 이미지 불러오기
            placeadapter.groupGalleryClickListener(object :
                GroupPlaceEventAdapter.GalleryInterface {
                override fun onGalleryClicked(
                    imgLists: ArrayList<String?>,
                    position: Int
                ) {
                    this@GroupModifyActivity.imgList = imgLists
                    this@GroupModifyActivity.positionForGallery = position

                    getPermission()
                }
            })
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
            placeEvent.add(DiaryGroupEvent("장소", 0, arrayListOf(), arrayListOf()))
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

}