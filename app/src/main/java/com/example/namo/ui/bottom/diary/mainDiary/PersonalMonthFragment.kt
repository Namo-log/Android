package com.example.namo.ui.bottom.diary.mainDiary

import DiaryAdapter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.databinding.FragmentDiaryPersonalMonthBinding
import kotlinx.coroutines.runBlocking

class PersonalMonthFragment : Fragment() {

    private var _binding: FragmentDiaryPersonalMonthBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: DiaryRepository
    private lateinit var diaryDateAdapter: DiaryAdapter

    var yearMonth: String = ""
    var currentPage = 0 // 초기 페이지
    val pageSize = 10 // 페이지 당 아이템 수
    var isLoading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            yearMonth = it.getString("yearMonth", "")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryPersonalMonthBinding.inflate(inflater, container, false)

        repo = DiaryRepository(requireContext())
        Log.d("yearMonthPersonal", yearMonth)

        diaryDateAdapter = DiaryAdapter(parentFragmentManager, requireContext())

        getDiaryList(currentPage, pageSize)

        onRecyclerview()

        onClickListener()

        return binding.root
    }


    private fun getDiaryList(page: Int, size: Int) {

        val diaryItems = runBlocking {
            repo.getDiaryList(yearMonth, page, size)  // 월 별 다이어리 조회
        }

        diaryDateAdapter.submitPersonalList(diaryItems)
        Log.d("diaryFragment", diaryItems.toString())

        // 달 별 메모 없으면 없다고 띄우기
        if (diaryItems.isNotEmpty()) {
            binding.diaryPersonalListRv.visibility = View.VISIBLE
        } else {
            binding.diaryPersonalListRv.visibility = View.GONE
            binding.diaryListEmptyTv.visibility = View.VISIBLE
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun onRecyclerview() {

        binding.diaryPersonalListRv.apply {
            adapter = diaryDateAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(java.lang.Boolean.TRUE)
        }

        binding.diaryPersonalListRv.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()

                    val itemTotalCount = recyclerView.adapter!!.itemCount


                    // 마지막 아이템이 보여지고, 로딩 중이 아닌 경우
                    if (lastVisibleItemPosition >= itemTotalCount - 1 && !isLoading) {
                        isLoading = true

                        if ((currentPage + 1) * pageSize < itemTotalCount) {
                            currentPage++
                            val offset = currentPage * pageSize

                            val runnable = Runnable {
                                getDiaryList(currentPage, offset)
                            }
                            recyclerView.post(runnable)
                        }

                        isLoading = false
                    }
                }

            })
        }
    }

    private fun onClickListener() {
        val r = Runnable {
            try {

                // 수정 버튼 클릭리스너
                diaryDateAdapter.setRecordClickListener { allData ->
                    val bundle = Bundle()

                    val event = Event(
                        allData.eventId,
                        allData.event_title,
                        allData.event_start, 0L, 0,
                        allData.event_category_idx, allData.event_place_name,
                        0.0, 0.0, 0, null, 1,
                        R.string.event_current_default.toString(),
                        allData.event_server_idx,
                        allData.event_category_server_idx
                    )

                    bundle.putSerializable("event", event)

                    val editFrag = DiaryModifyFragment()
                    editFrag.arguments = bundle
                    view?.findNavController()
                        ?.navigate(R.id.action_diaryFragment_to_diaryModifyFragment, bundle)
                }

            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(r)
        thread.start()

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