package com.example.namo.ui.bottom.diary.mainDiary


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

class GroupDetailFragment : Fragment(), DiaryBasicView {

    private var _binding: FragmentDiaryGroupDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupSchedule:DiaryResponse.MonthDiary
    private lateinit var diaryService: DiaryService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupDetailBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        groupSchedule=requireArguments().getSerializable("groupDiary") as DiaryResponse.MonthDiary
        diaryService = DiaryService()

        bind()
        editMemo()

        return binding.root
    }

    private fun bind() {

        binding.diaryBackIv.setOnClickListener {
            findNavController().popBackStack()
            hideBottomNavigation(false)
        }

        binding.groupDiaryDetailLy.setOnClickListener {

            val bundle = Bundle()
            bundle.putLong("groupScheduleId", groupSchedule.scheduleIdx)
            findNavController().navigate(
                R.id.action_groupDetailFragment_to_groupMemoActivity,
                bundle
            )
        }

        binding.diaryDeleteIv.setOnClickListener {
            binding.diaryContentsEt.text.clear()

            diaryService.addGroupAfterDiary(groupSchedule.scheduleIdx, "")
            diaryService.diaryBasicView(this)
        }

        val repo=DiaryRepository(requireContext())
        val categoryId = requireArguments().getLong("categoryIdx", 0L)
        val category = repo.getCategory(categoryId,categoryId)
        context?.resources?.let {
            binding.itemDiaryCategoryColorIv.background.setTint(category.color)
        }

        val date = DateTime(groupSchedule.startDate * 1000).toString("yyyy.MM.dd (EE)")
        binding.diaryTitleTv.text = groupSchedule.title
        binding.diaryInputDateTv.text = date
        binding.diaryInputPlaceTv.text = groupSchedule.placeName

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

        galleryViewRVAdapter.addImages(groupSchedule.imgUrl)

        binding.diaryContentsEt.setText(groupSchedule.content)
    }

    private fun editMemo() {

       val  content = binding.diaryContentsEt.text.toString()

        binding.diaryEditTv.setOnClickListener {

            diaryService.addGroupAfterDiary(groupSchedule.scheduleIdx, binding.diaryContentsEt.text.toString())
            diaryService.diaryBasicView(this)
            findNavController().popBackStack()
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