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
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryGroupDetailBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime

class GroupDetailFragment : Fragment(), GetGroupDiaryView, DiaryBasicView {

    private var _binding: FragmentDiaryGroupDetailBinding? = null
    private val binding get() = _binding!!

    private var groupScheduleId: Long = 0L
    private lateinit var diaryService: DiaryService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupDetailBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        groupScheduleId = requireArguments().getLong("groupScheduleId", 0L)

        diaryService = DiaryService()
        diaryService.getGroupDiary(groupScheduleId)
        diaryService.getGroupDiaryView(this)


        bind()
        editMemo()

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
        Log.d("Error", message)
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

        binding.diaryDeleteIv.setOnClickListener {
            binding.diaryContentsEt.text.clear()

            diaryService.addGroupAfterDiary(groupScheduleId, "")
            diaryService.diaryBasicView(this)
        }

        val repo=DiaryRepository(requireContext())
        val categoryId = requireArguments().getLong("categoryIdx", 0L)
        val category = repo.getCategory(categoryId,categoryId)
        context?.resources?.let {
            binding.itemDiaryCategoryColorIv.background.setTint(category.color)
        }
    }

    private fun editMemo() {

       val  content = binding.diaryContentsEt.text.toString()

        binding.diaryEditTv.setOnClickListener {

            diaryService.addGroupAfterDiary(groupScheduleId, binding.diaryContentsEt.text.toString())
            diaryService.diaryBasicView(this)
            findNavController().popBackStack()
            Log.d("sdfe",content)
        }

        if (content.isEmpty()) {
            binding.diaryDeleteIv.visibility = View.GONE
            binding.diaryEditTv.text = "기록 저장"
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.MainOrange)

        } else {
            binding.diaryDeleteIv.visibility = View.VISIBLE
            binding.diaryEditTv.text = "기록 수정"
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.MainOrange
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.white)
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

    override fun onSuccess(response: DiaryResponse.DiaryResponse) {
        Log.e("addGroupDiaryAfter", response.toString())
    }

    override fun onFailure(message: String) {
        Log.e("addGroupDiaryAfter", message)
    }


}