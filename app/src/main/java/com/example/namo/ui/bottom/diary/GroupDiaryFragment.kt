package com.example.namo.ui.bottom.diary

import android.annotation.SuppressLint
import android.content.Intent
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

class GroupDiaryFragment : Fragment() {

    private var _binding: FragmentDiaryGroupAddBinding? = null
    private val binding get() = _binding!!
    private var memberNames=ArrayList<GroupDiaryMember>()
    private var placeEvent= mutableListOf<DiaryGroupEvent>()
    private lateinit var adapter:GroupPlaceEventAdapter


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentDiaryGroupAddBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        binding.groupAddBackIv.setOnClickListener { findNavController().popBackStack() }
        onRecyclerView()
        onClickListener()
        dummy()

        return binding.root
    }

    private fun onRecyclerView(){

        binding.apply {

            // 멤버 이름 리사이클러뷰
            val groupMemberAT=GroupMemberRVAdapter(memberNames)
            groupAddPeopleRv.adapter=groupMemberAT
            groupAddPeopleRv.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)


            // 장소 추가 리사이클러뷰
            adapter=GroupPlaceEventAdapter(placeEvent)
            diaryGroupAddPlaceRv.adapter=adapter
            diaryGroupAddPlaceRv.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
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
        binding.groudPlaceAddTv.setOnClickListener {
            addPlace()

            adapter.notifyDataSetChanged()

        }

    }

    private fun addPlace(){


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


        placeEvent.apply {
            add(DiaryGroupEvent(0,"",0, listOf(), listOf(),false))
        }
        }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }
}