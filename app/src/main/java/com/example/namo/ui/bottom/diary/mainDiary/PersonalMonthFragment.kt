package com.example.namo.ui.bottom.diary.mainDiary

import DiaryAdapter
import DiaryItem
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.databinding.FragmentDiaryPersonalMonthBinding


class PersonalMonthFragment : Fragment() {

    private var _binding: FragmentDiaryPersonalMonthBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: DiaryRepository

    var yearMonth: String = ""
    var currentPage = 0 // 초기 페이지
    private val pageSize = 7 // 페이지 당 아이템 수

    private lateinit var diaryAdapter: DiaryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            yearMonth = it.getString("yearMonth", "")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryPersonalMonthBinding.inflate(inflater, container, false)

        repo = DiaryRepository(requireContext())

        Log.d("yearMonthPersonal", yearMonth)

        diaryAdapter = DiaryAdapter(editClickListener = {
            onClickListener(it)
        }, imageClickListener = {
            ImageDialog(it).show(parentFragmentManager, "test")
        })

        getDiaryList(currentPage, pageSize)

        return binding.root
    }

    private fun getDiaryList(page: Int, size: Int) {

        val storeDB = Thread {
            val diaryItems = repo.getDiaryList(yearMonth, page, size)  // 월 별 다이어리 조회

            Log.d("ewr",diaryItems.toString())
            requireActivity().runOnUiThread {
                diaryAdapter.updateData(diaryItems)

                // 달 별 메모 없으면 없다고 띄우기
                if (diaryItems.isNotEmpty()) {
                    binding.diaryPersonalListRv.visibility = View.VISIBLE
                } else {
                    binding.diaryPersonalListRv.visibility = View.GONE
                    binding.diaryListEmptyTv.visibility = View.VISIBLE
                }
            }
        }

        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onRecyclerview()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun onRecyclerview() {

        binding.diaryPersonalListRv.apply {
            adapter = diaryAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
//
//        binding.diaryPersonalListRv.apply {
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//
//                    val lastVisibleItemPosition =
//                        (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
//
//                    val itemTotalCount = recyclerView.adapter!!.itemCount
//
//
//                    // 마지막 아이템이 보여지고, 로딩 중이 아닌 경우
//                    if (lastVisibleItemPosition >= itemTotalCount - 1 && !isLoading) {
//                        isLoading = true
//
//                        if ((currentPage + 1) * pageSize < itemTotalCount) {
//                            currentPage++
//                            val offset = currentPage * pageSize
//
//                            getDiaryList(currentPage, offset)
//
//
//                        }
//
//                        isLoading = false
//                    }
//                }
//
//            })
//        }
    }

    private fun onClickListener(item: DiaryItem.Content) {

        // 수정 버튼 클릭리스너

        val bundle = Bundle()

        val event = Event(
            item.eventId,
            item.event_title,
            item.event_start, 0L, 0,
            item.event_category_idx, item.event_place_name,
            0.0, 0.0, 0, null, 1,
            R.string.event_current_default.toString(),
            item.event_server_idx,
            item.event_category_server_idx
        )

        bundle.putSerializable("event", event)

        val editFrag = DiaryModifyFragment()
        editFrag.arguments = bundle
        view?.findNavController()
            ?.navigate(R.id.action_diaryFragment_to_diaryModifyFragment, bundle)

    }

    companion object {
        fun newInstance(yearMonth: String) = PersonalMonthFragment().apply {
            arguments = Bundle().apply {
                putString("yearMonth", yearMonth)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}