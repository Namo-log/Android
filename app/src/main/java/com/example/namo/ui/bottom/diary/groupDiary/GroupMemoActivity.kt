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
import com.example.namo.data.remote.diary.*
import com.example.namo.data.remote.moim.MoimSchedule
import com.example.namo.databinding.ActivityDiaryGroupMemoBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPlaceEventAdapter
import com.example.namo.utils.ConfirmDialog
import com.example.namo.utils.ConfirmDialogInterface
import org.joda.time.DateTime
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroupMemoActivity : AppCompatActivity(), GetGroupDiaryView, DiaryBasicView,
    ConfirmDialogInterface {  // 그룹 다이어리 추가, 수정, 삭제 화면

    private lateinit var binding: ActivityDiaryGroupMemoBinding

    private lateinit var memberadapter: GroupMemberRVAdapter  // 그룹 멤버 리스트 보여주기
    private lateinit var placeadapter: GroupPlaceEventAdapter // 각 장소 item

    private lateinit var groupMembers: List<DiaryResponse.GroupUser>
    private lateinit var groupData: DiaryResponse.GroupDiaryResult

    private lateinit var memberIntList: List<Long>
    private lateinit var repo: DiaryRepository
    private lateinit var moimSchedule: MoimSchedule

    private var groupEvent = emptyList<DiaryResponse.LocationDto>()
    private var placeEvent = ArrayList<DiaryGroupEvent>()

    private var imgList: ArrayList<String?> = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1
    private var groupScheduleId: Long = 0L

    private val itemTouchSimpleCallback = ItemTouchHelperCallback()  // 아이템 밀어서 삭제
    private val itemTouchHelper = ItemTouchHelper(itemTouchSimpleCallback)


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
            moimSchedule = intent?.getSerializableExtra("groupEvent") as MoimSchedule
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

            addPlace()

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

            editPlace()
            deletePlace()
        }

    }


    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    override fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse) {
        Log.d("GET_GROUP_DIARY", response.toString())

        val result = response.result
        groupMembers = result.users
        groupData = result
        groupEvent = result.locationDtos

        memberIntList = groupMembers.map { it.userId }

        placeEvent.addAll(groupEvent.map {
            val copy = it.copy(
                imgs = it.imgs.toMutableList()
            )
            DiaryGroupEvent(
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

        // 그룹 장소가 없을 떄, 그룹 스케줄에서 가져온 데이터 바인딩
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

        val members = arrayListOf<DiaryResponse.GroupUser>()
        moimSchedule.users.map {
            members.add(DiaryResponse.GroupUser(it.userId, it.userName))
        }

        groupMembers = members
        memberIntList = groupMembers.map { it.userId }

        onRecyclerView()

    }

    private fun addPlace() {

        binding.groupSaveTv.setOnClickListener { // 저장

            placeEvent.forEach {
                repo.addMoimDiary(
                    groupScheduleId,
                    it.place.ifEmpty { "장소" },
                    it.pay,
                    it.members,
                    it.imgs as List<String>?
                )
            }
            finish()
        }
    }

    private fun editPlace() {


        binding.groupSaveTv.setOnClickListener {  // 수정

            placeEvent.forEach { diaryGroupEvent ->
                val hasDiffer = groupEvent.any { group ->
                    group.place == diaryGroupEvent.place &&
                            group.pay == diaryGroupEvent.pay &&
                            group.members == diaryGroupEvent.members &&
                            group.imgs == diaryGroupEvent.imgs.filterNotNull()
                }


                if (!hasDiffer) {
                    if (diaryGroupEvent.placeIdx == 0L) {
                        repo.addMoimDiary(
                            groupScheduleId,
                            diaryGroupEvent.place.ifEmpty { "장소" },
                            diaryGroupEvent.pay,
                            diaryGroupEvent.members,
                            diaryGroupEvent.imgs.filterNotNull()
                        )
                        repo.diaryService.diaryBasicView(this)
                    } else {
                        repo.editGroupPlace(
                            diaryGroupEvent.placeIdx,
                            diaryGroupEvent.place.ifEmpty { "장소" },
                            diaryGroupEvent.pay,
                            diaryGroupEvent.members,
                            diaryGroupEvent.imgs.filterNotNull()
                        )
                        repo.diaryService.diaryBasicView(this)
                    }
                }

            }
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
        // 모임 기록 전체 삭제
        placeEvent.forEach {
            val diaryService = DiaryService()
            diaryService.deleteGroupDiary(it.placeIdx)
            diaryService.diaryBasicView(this)
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
                        payText.text = NumberFormat.getNumberInstance(Locale.US).format(it)

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

            if (placeEvent.size >= 3) Toast.makeText(this, "장소 추가는 3개까지 가능합니다", Toast.LENGTH_SHORT)
                .show()
            else {
                placeEvent.add(DiaryGroupEvent("", 0, arrayListOf(), arrayListOf()))
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
            placeEvent[position].imgs = images
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

    override fun onSuccess(response: DiaryResponse.DiaryResponse) {
        finish()
        Log.d("delete_group_diary", "SUCCESS")
    }

    override fun onFailure(message: String) {
        finish()
        Log.d("delete_group_diary", message)
    }

}