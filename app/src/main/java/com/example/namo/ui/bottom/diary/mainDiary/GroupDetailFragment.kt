package com.example.namo.ui.bottom.diary.mainDiary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.databinding.FragmentDiaryGroupDetailBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGroupItem
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime

class GroupDetailFragment : Fragment() {

    private var _binding: FragmentDiaryGroupDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupItem: DiaryGroupItem.Content

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupDetailBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        groupItem = (arguments?.getSerializable("groupDiaryItem") as? DiaryGroupItem.Content)!!
        Log.e("scheduleIdx",groupItem.eventId.toString())
        bind()

        return binding.root
    }

    private fun bind() {

        val date = DateTime(groupItem.event_start * 1000).toString("yyyy.MM.dd (EE)")
        binding.diaryTitleTv.text = groupItem.event_title
        binding.diaryInputDateTv.text = date
        binding.diaryInputPlaceTv.text = groupItem.event_place_name


        val galleryViewRVAdapter = GalleryListAdapter(requireContext())
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        groupItem.images?.let { galleryViewRVAdapter.addImages(it) }

        binding.diaryBackIv.setOnClickListener {
            findNavController().popBackStack()
            hideBottomNavigation(false)
        }

        binding.groupDiaryDetailLy.setOnClickListener {

            val bundle = Bundle()
            bundle.putLong("groupScheduleId", groupItem.eventId)
            findNavController().navigate(R.id.action_groupDetailFragment_to_groupMemoActivity, bundle)
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