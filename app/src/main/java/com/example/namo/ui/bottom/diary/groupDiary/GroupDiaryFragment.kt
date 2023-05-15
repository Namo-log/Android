package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.FragmentDiaryGroupAddBinding
import com.example.namo.ui.bottom.diary.adapter.GroupMemberRVAdapter
import com.example.namo.ui.bottom.diary.adapter.GroupPlaceEventAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class GroupDiaryFragment : Fragment() {  // 그룹 다이어리 추가 화면

    private var _binding: FragmentDiaryGroupAddBinding? = null
    private val binding get() = _binding!!

    private var memberNames=ArrayList<GroupDiaryMember>()  // 그룹 다이어리 구성원
    private var placeEvent: MutableList<DiaryGroupEvent> =mutableListOf() // 장소, 정산 금액, 이미지

    private lateinit var memberadapter:GroupMemberRVAdapter
    private lateinit var placeadapter:GroupPlaceEventAdapter
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

        initialize() // placeEvent 초기화

        onRecyclerView()
        onClickListener()
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
            placeadapter.groupPayClickListener(object :GroupPlaceEventAdapter.PayInterface{
                override fun onPayClicked() {
                    GroupPayDialog(memberNames).show(parentFragmentManager,"show")
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onClickListener(){

        binding.bottomArrow.setOnClickListener{
            setMember(false)
        }
        binding.upArrow.setOnClickListener{
            setMember(true)
        }
        binding.groupAddBackIv.setOnClickListener{
            findNavController().popBackStack()
        }

        // 장소 추가 버튼 클릭리스너
        binding.groudPlaceAddTv.setOnClickListener {
            val string="장소 $i"
            i++
            placeEvent.add(DiaryGroupEvent(string))
            placeadapter.notifyDataSetChanged()
        }
    }

    private fun initialize(){
        with(placeEvent){
            add(DiaryGroupEvent("장소 1"))
        }
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