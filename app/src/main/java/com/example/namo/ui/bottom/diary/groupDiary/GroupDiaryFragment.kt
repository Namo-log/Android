package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.FragmentDiaryGroupAddBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPlaceEventAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class GroupDiaryFragment : Fragment() {  // 그룹 다이어리 추가 화면

    private var _binding: FragmentDiaryGroupAddBinding? = null
    private val binding get() = _binding!!

    private var memberNames=ArrayList<GroupDiaryMember>()  // 그룹 다이어리 구성원
    private var placeEvent: MutableList<DiaryGroupEvent> =mutableListOf() // 장소, 정산 금액, 이미지
    private var imgList= arrayListOf<String>() // 장소별 이미지
   // private lateinit var imgList:ArrayList<String>
    private var groupPay:Int=0

    private lateinit var memberadapter: GroupMemberRVAdapter
    private lateinit var placeadapter: GroupPlaceEventAdapter

    var i=2
    init{
        instance = this
    }
    companion object{
        private var instance:GroupDiaryFragment? = null
        fun getInstance(): GroupDiaryFragment? {
            return instance
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentDiaryGroupAddBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        onRecyclerView()
        onClickListener()
        initialize() // placeEvent 초기화
        dummy()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onRecyclerView(){

        binding.apply {

            // 멤버 이름 리사이클러뷰
            memberadapter= GroupMemberRVAdapter(memberNames)
            groupAddPeopleRv.adapter=memberadapter
            groupAddPeopleRv.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)


            // 장소 추가 리사이클러뷰
            placeadapter= GroupPlaceEventAdapter(requireContext(),placeEvent)
            diaryGroupAddPlaceRv.adapter=placeadapter
            diaryGroupAddPlaceRv.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

            // 정산 다이얼로그
            placeadapter.groupPayClickListener(object : GroupPlaceEventAdapter.PayInterface{
                override fun onPayClicked(pay:Int) {
                    GroupPayDialog(memberNames){
                        groupPay=it
                    }.show(parentFragmentManager,"show")
                }
            })

            // 이미지 불러오기
            placeadapter.groupGalleryClickListener(object : GroupPlaceEventAdapter.GalleryInterface{
                override fun onGalleryClicked() {
                    getPermission()
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onClickListener(){

        binding.upArrow.setOnClickListener{// 화살표가 위쪽 방향일 때 리사이클러뷰 숨쪽기
            setMember(true)
        }

        binding.bottomArrow.setOnClickListener{
            setMember(false)
        }

        binding.groupAddBackIv.setOnClickListener{ // 뒤로가기
            findNavController().popBackStack()
        }

        binding.groupPlaceSaveTv.setOnClickListener {// 저장하기
            findNavController().popBackStack()
        }

        // 장소 추가 버튼 클릭리스너
        binding.groudPlaceAddTv.setOnClickListener {
            val string="장소 $i"
            i++
            placeEvent.add(DiaryGroupEvent(string,groupPay,imgList))
            placeadapter.notifyDataSetChanged()
        }
    }

    private fun initialize(){
        with(placeEvent){
            add(DiaryGroupEvent("장소 1",groupPay, imgList))
        }
        Log.d("imglst",imgList.toString())
    }

    private fun setMember(isVisible: Boolean) {
        if (isVisible) {
            binding.groupAddPeopleRv.visibility= View.GONE
            binding.bottomArrow.visibility= View.VISIBLE
            binding.upArrow.visibility= View.GONE

        } else {
            binding.groupAddPeopleRv.visibility= View.VISIBLE
            binding.bottomArrow.visibility= View.GONE
            binding.upArrow.visibility= View.VISIBLE
        }
    }

    @SuppressLint("IntentReset")
    private fun getPermission(){

        val writePermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),200)
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

    private val getImage=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result->

        if ( result.resultCode == Activity.RESULT_OK) {
            imgList.clear()
            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3)  {
                    Toast.makeText(requireContext(), "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri
                        val file = File(absolutelyPath(imageUri, requireContext()))
                        imgList.add(imageUri.toString())

                    }
                }
            }
        } else { // 단일 선택
            result.data?.data?.let {
                val imageUri : Uri? = result.data!!.data
                if (imageUri != null) {
                    val  file = File(absolutelyPath(imageUri, requireContext()))
                    imgList.add(imageUri.toString())
                }
            }
        }
        placeadapter.addItem(imgList)

    }

    /** 이미지 절대 경로 변환 **/
    @SuppressLint("Recycle")
    private fun absolutelyPath(path: Uri, context: Context): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context.contentResolver.query(path, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        val result = c?.getString(index!!)

        return result!!
    }

    private fun hideBottomNavigation( bool : Boolean){
        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
        if(bool) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    fun dummy(){
        memberNames.apply {
            add(GroupDiaryMember("코코아"))
            add(GroupDiaryMember("지니"))
            add(GroupDiaryMember("앨리")) }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }
}