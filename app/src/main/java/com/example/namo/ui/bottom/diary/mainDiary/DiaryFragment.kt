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
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryBinding
import org.joda.time.DateTime
import java.lang.Boolean.TRUE

class DiaryFragment : Fragment(), DiaryRepository.DiaryCallback {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private lateinit var repo: DiaryRepository

    private lateinit var diaryDateAdapter: DiaryAdapter
    private lateinit var yearMonth: String

    var currentPage = 0 // 초기 페이지
    val pageSize = 7 // 페이지 당 아이템 수
    var isLoading = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        binding.diaryMonth.text = DateTime(dateTime).toString("yyyy.MM")
        yearMonth = binding.diaryMonth.text.toString()

        repo = DiaryRepository(requireContext())
        repo.setCallBack(this)

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        diaryDateAdapter = DiaryAdapter(parentFragmentManager, requireContext())
        onRecyclerview()

        // 그룹 다이어리 테스트, 확인하고 지우기....
        binding.groupdiarytest.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_groupDiaryFragment)
        }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        diaryDateAdapter.notifyDataSetChanged()
    }

    override fun onGetDiaryItems(diaryItem: List<DiaryItem>) {

        diaryDateAdapter.submitList(diaryItem)
        getList(diaryItem)

        Log.d("checkDiaryList", diaryItem.toString())
    }


    @SuppressLint("NotifyDataSetChanged")
    fun onRecyclerview() {

        binding.diaryListRv.apply {
            adapter = diaryDateAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(TRUE)
            (adapter as DiaryAdapter).notifyDataSetChanged()
        }

        binding.diaryListRv.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()

                //    val firstVisibleItemPosition=(recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter!!.itemCount


                    // 마지막 아이템이 보여지고, 로딩 중이 아닌 경우
                    if (lastVisibleItemPosition >= itemTotalCount -1 && !isLoading) {
                        isLoading = true

                        if ((currentPage + 1) * pageSize < itemTotalCount) {
                            currentPage++
                            val offset = currentPage * pageSize
                            repo.getDiaryList(yearMonth, currentPage, offset)
                        }

                        isLoading = false
                    }

                    // 첫 번째 아이템이 보여지고, 로딩 중이 아닌 경우
//                    if (firstVisibleItemPosition == 0 && !isLoading) {
//                        isLoading = true
//
//                        if (currentPage > 0) {
//                            currentPage--
//                            val offset = currentPage * pageSize
//                            repo.getDiaryList(yearMonth, currentPage, offset)
//                        }
//
//                        isLoading = false
//                    }
                }

            })

            repo.getDiaryList(yearMonth, currentPage, pageSize)
        }
    }


    private fun getList(diaryItems: List<DiaryItem>) {
        val r = Runnable {
            try {

                // 수정 버튼 클릭리스너
                diaryDateAdapter.setRecordClickListener(object : DiaryAdapter.DiaryEditInterface {
                    override fun onEditClicked(allData: DiaryItem.Content) {
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
                })

                requireActivity().runOnUiThread {

                    // 달 별 메모 없으면 없다고 띄우기
                    if (diaryItems.isNotEmpty()) {
                        binding.diaryListRv.visibility = View.VISIBLE
                    } else {
                        binding.diaryListRv.visibility = View.GONE
                        binding.diaryListEmptyTv.visibility = View.VISIBLE
                    }

                }
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(r)
        thread.start()

    }


    /** 다이얼로그 띄우기 **/
    private fun dialogCreate() {

        YearMonthDialog(dateTime) {
            yearMonth = DateTime(it).toString("yyyy.MM")
            binding.diaryMonth.text = yearMonth

            onRecyclerview()

        }.show(parentFragmentManager, "test")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}