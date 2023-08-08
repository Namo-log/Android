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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.utils.NetworkManager
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import java.lang.Boolean.TRUE

class DiaryFragment : Fragment() {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private lateinit var repo: DiaryRepository

    private lateinit var diaryDateAdapter: DiaryAdapter
    private lateinit var yearMonth: String

    private var isGroupDiary: Boolean = false

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

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        diaryDateAdapter = DiaryAdapter(parentFragmentManager, requireContext())

        getDiaryList()

        loadDiaryList(currentPage,pageSize)

        onRecyclerview()
        Log.d("ewewe2",isGroupDiary.toString())

        onClickListener()

        // 그룹 다이어리 테스트, 확인하고 지우기....
        binding.groupdiarytest.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_groupDiaryFragment)
        }
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    private fun getDiaryList(){

        binding.diarySwitchBtn.setOnCheckedChangeListener { _, isChecked ->
            isGroupDiary = isChecked

            if(!isChecked){
                binding.diarySwitchBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textGray))
            }
            else {
                binding.diarySwitchBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }

        }
    }


    private fun loadDiaryList(page: Int, size: Int) {
        if (isGroupDiary) {
            if (NetworkManager.checkNetworkState(requireContext())) {
                getGroupDiaryList(page, size)
            } else {
                binding.diaryListEmptyTv.visibility = View.VISIBLE
            }
        } else {

            getPersonalDiaryList(page, size)
        }
        Log.d("ewer",isGroupDiary.toString())
    }


    private fun getPersonalDiaryList(page: Int, size: Int) {

        val diaryItems = runBlocking {
            repo.getDiaryList(yearMonth, page, size)  // 월 별 다이어리 조회
        }

        diaryDateAdapter.submitPersonalList(diaryItems)
        Log.d("s",diaryItems.toString())

        // 달 별 메모 없으면 없다고 띄우기
        if (diaryItems.isNotEmpty()) {
            binding.diaryListRv.visibility = View.VISIBLE
        } else {
            binding.diaryListRv.visibility = View.GONE
            binding.diaryListEmptyTv.visibility = View.VISIBLE
        }
    }

    private fun getGroupDiaryList(page: Int,size: Int){

        // adapter 새로 만들어서 서버에서 가져오기
        binding.diaryListEmptyTv.visibility = View.VISIBLE

        diaryDateAdapter.submitGroupList()
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

                    val itemTotalCount = recyclerView.adapter!!.itemCount


                    // 마지막 아이템이 보여지고, 로딩 중이 아닌 경우
                    if (lastVisibleItemPosition >= itemTotalCount - 1 && !isLoading) {
                        isLoading = true

                        if ((currentPage + 1) * pageSize < itemTotalCount) {
                            currentPage++
                            val offset = currentPage * pageSize

                          //  getDiaryList(currentPage,offset)
                            loadDiaryList(currentPage,offset)
                        }

                        isLoading = false
                    }
                }

            })
        }
    }

    /** 다이얼로그 띄우기 **/
    private fun dialogCreate() {

        YearMonthDialog(dateTime) {
            yearMonth = DateTime(it).toString("yyyy.MM")
            binding.diaryMonth.text = yearMonth

            onRecyclerview()
        }.show(parentFragmentManager, "test")

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




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}