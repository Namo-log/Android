package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.data.remote.diary.DiaryService
import com.example.namo.data.remote.diary.GetGroupDiaryView
import com.example.namo.databinding.FragmentDiaryGroupModifyBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPlaceEventAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat

class GroupModifyFragment : Fragment(), GetGroupDiaryView {  // 그룹 다이어리 추가 화면

    private var _binding: FragmentDiaryGroupModifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var memberadapter: GroupMemberRVAdapter
    private lateinit var placeadapter: GroupPlaceEventAdapter

    private var memberNames = ArrayList<String>()  // 그룹 다이어리 구성원
    private var imgList: ArrayList<String?> = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1

    private var placeEvent = ArrayList<DiaryGroupEvent>()
    private lateinit var groupMembers: List<DiaryResponse.GroupUser>
    private lateinit var groupData: DiaryResponse.GroupDiaryResult
    private lateinit var groupEvent: List<DiaryResponse.LocationDto>
    private lateinit var memberIntList: List<Int>
    private lateinit var repo: DiaryRepository
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupModifyBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        repo = DiaryRepository(requireContext())

        val diaryService = DiaryService()
        diaryService.getGroupDiary(7)
        diaryService.getGroupDiaryView(this)

        onClickListener()
        dummy()

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse) {
        Log.d("GET_GROUP_DIARY", response.toString())

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

        onRecyclerView()
    }

    override fun onGetGroupDiaryFailure(message: String) {
        Log.d("GET_GROUP_DIARY", message)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onRecyclerView() {

        binding.apply {

            // 멤버 이름 리사이클러뷰
            memberadapter = GroupMemberRVAdapter(groupMembers)
            groupAddPeopleRv.adapter = memberadapter
            groupAddPeopleRv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // 장소 추가 리사이클러뷰
            placeadapter = GroupPlaceEventAdapter(requireContext(), placeEvent)
            diaryGroupAddPlaceRv.adapter = placeadapter
            diaryGroupAddPlaceRv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            val itemTouchHelperCallback = ItemTouchHelperCallback(placeadapter)
            val helper = ItemTouchHelper(itemTouchHelperCallback)
            // RecyclerView에 ItemTouchHelper 연결
            helper.attachToRecyclerView(binding.diaryGroupAddPlaceRv)

            // 정산 다이얼로그
            placeadapter.groupPayClickListener(object : GroupPlaceEventAdapter.PayInterface {
                override fun onPayClicked(
                    pay: Int,
                    position: Int,
                    payText: TextView
                ) {
                    GroupPayDialog(groupMembers, placeEvent[position], {
                        placeEvent[position].pay = it
                        payText.text = it.toString()

                    }, {
                        placeEvent[position].members = it
                    }).show(parentFragmentManager, "show")
                }
            })


            // 이미지 불러오기
            placeadapter.groupGalleryClickListener(object :
                GroupPlaceEventAdapter.GalleryInterface {
                override fun onGalleryClicked(
                    imgLists: ArrayList<String?>,
                    position: Int
                ) {
                    this@GroupModifyFragment.imgList = imgLists
                    this@GroupModifyFragment.positionForGallery = position

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
            findNavController().popBackStack(R.id.diaryFragment, true)
        }

        binding.groupModifySaveTv.setOnClickListener {// 저장하기

            for (placeEventItem in placeEvent) {

                repo.editGroupPlace(
                    placeEventItem.placeIdx,
                    placeEventItem.place,
                    placeEventItem.pay,
                    placeEventItem.members,
                    placeEventItem.imgs as List<String>?
                )

            }

        }

        // 장소 추가 버튼 클릭리스너
//        binding.groudPlaceAddTv.setOnClickListener {
//            placeEvent.add(DiaryGroupEvent("장소", 0, arrayListOf(), arrayListOf()))
//            placeadapter.notifyDataSetChanged()
//        }
        binding.groudPlaceAddTv.visibility = View.GONE
        binding.groudPlaceAddIv.visibility = View.GONE
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
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
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
                    Toast.makeText(requireContext(), "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
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

            placeadapter.addItem(images)
        }
    }

    private fun hideBottomNavigation(bool: Boolean) {
        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.nav_bar)
        if (bool) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    private fun dummy() {
        memberNames.apply {
            add("코코아")
            add("지니")
            add("앨리")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }


}