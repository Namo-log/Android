package com.example.namo.ui.bottom.diary.mainDiary

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.data.remote.diary.DiaryService
import com.example.namo.data.remote.diary.GetGroupDiaryView
import com.example.namo.databinding.FragmentDiaryGroupDetailBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime

class GroupDetailFragment : Fragment(), GetGroupDiaryView {

    private var _binding: FragmentDiaryGroupDetailBinding? = null
    private val binding get() = _binding!!

    private var groupScheduleId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupDetailBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        groupScheduleId = requireArguments().getLong("groupScheduleId", 0L)

        val diaryService = DiaryService()
        diaryService.getGroupDiary(groupScheduleId)
        diaryService.getGroupDiaryView(this)

        bind()
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse) {

        val groupData = response.result

        val date = DateTime(groupData.startDate * 1000).toString("yyyy.MM.dd (EE)")
        binding.diaryTitleTv.text = groupData.name
        binding.diaryInputDateTv.text = date
        binding.diaryInputPlaceTv.text = groupData.locationName

        val galleryViewRVAdapter = GalleryListAdapter(requireContext())
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.itemDiaryCategoryColorIv.background.setTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.MainOrange
            )
        )

        val groupEvent = groupData.locationDtos
        galleryViewRVAdapter.addImages(getImagesByLocationId(groupEvent))

    }

    private fun getImagesByLocationId(locationDto: List<DiaryResponse.LocationDto>): List<String> {
        val imagesList = mutableListOf<String>()

        for (locationDtos in locationDto) {
            val urls = locationDtos.imgs.take(3) // 최대 3개의 이미지 가져오기
            imagesList.addAll(urls)

        }
        return imagesList
    }

    override fun onGetGroupDiaryFailure(message: String) {
        Log.d("Error",message)
    }

    private fun bind() {

        binding.diaryBackIv.setOnClickListener {
            findNavController().popBackStack()
            hideBottomNavigation(false)
        }

        binding.groupDiaryDetailLy.setOnClickListener {

            val bundle = Bundle()
            bundle.putLong("groupScheduleId", groupScheduleId)
            findNavController().navigate(
                R.id.action_groupDetailFragment_to_groupMemoActivity,
                bundle
            )
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

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }


}