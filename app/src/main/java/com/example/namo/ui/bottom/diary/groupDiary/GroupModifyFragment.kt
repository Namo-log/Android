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
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.FragmentDiaryGroupModifyBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPlaceEventAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class GroupModifyFragment : Fragment() {  // 그룹 다이어리 추가 화면

    private var _binding: FragmentDiaryGroupModifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var memberadapter: GroupMemberRVAdapter
    private lateinit var placeadapter: GroupPlaceEventAdapter

    private var memberNames = ArrayList<GroupDiaryMember>()  // 그룹 다이어리 구성원
    private lateinit var placeEvent: MutableList<DiaryGroupEvent>
    private var imgList: ArrayList<String?> = ArrayList() // 장소별 이미지
    private var positionForGallery: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupModifyBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        placeEvent = arguments?.getSerializable("groupDiary") as ArrayList<DiaryGroupEvent>
        onRecyclerView()
        onClickListener()
        dummy()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onRecyclerView() {

        binding.apply {

            // 멤버 이름 리사이클러뷰
            memberadapter = GroupMemberRVAdapter(memberNames)
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
                    GroupPayDialog(memberNames) {
                        if (position != RecyclerView.NO_POSITION) {
                            placeEvent[position].pay = it
                            payText.text = it.toString()
                        }
                    }.show(parentFragmentManager, "show")
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
            findNavController().popBackStack(R.id.diaryFragment, true)
            Log.d("placeEvent", "$placeEvent")
        }

        // 장소 추가 버튼 클릭리스너
        binding.groudPlaceAddTv.setOnClickListener {
            placeEvent.add(DiaryGroupEvent("장소", 0, arrayListOf()))
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
            val intent = Intent()
            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기
            intent.action = Intent.ACTION_GET_CONTENT

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
            add(GroupDiaryMember("코코아"))
            add(GroupDiaryMember("지니"))
            add(GroupDiaryMember("앨리"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }
}