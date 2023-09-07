package com.example.namo.ui.bottom.diary.mainDiary


import DiaryAdapter
import DiaryItem
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.data.remote.diary.DiaryService
import com.example.namo.data.remote.diary.GetGroupMonthView
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGroupAdapter
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGroupItem
import com.example.namo.utils.NetworkManager
import com.example.namo.databinding.FragmentDiaryBinding
import org.joda.time.DateTime


class DiaryFragment : Fragment(), GetGroupMonthView {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var currentYearMonth: String = ""

    private lateinit var sf: SharedPreferences
    private lateinit var repo: DiaryRepository
    private lateinit var diaryGroupAdapter: DiaryGroupAdapter

    private var yearMonthTextView: String = ""
    private var checked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        yearMonthTextView = DateTime(dateTime).toString("yyyy.MM")
        sf = requireContext().getSharedPreferences("sf", Context.MODE_PRIVATE)

        val savedString = sf.getString("yearMonth", "")
        if (!savedString.isNullOrEmpty()) yearMonthTextView = savedString

        binding.diaryMonth.text = yearMonthTextView

        repo = DiaryRepository(requireContext())

        checkSwitchBtn()
        dialogCreate()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        getList()
    }

    private fun getList() {

        if (!checked) {  // 개인 기록 가져오기
            binding.switchOnOff.isChecked = false
            getPersonalList()

        } else { // 그룹 기록 가져오기
            binding.switchOnOff.isChecked = true
            getGroupList()
        }
    }

    private fun checkSwitchBtn() {

        binding.switchOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) { // 개인 기록
                binding.personalBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.groupBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textGray
                    )
                )

                checked = false
                getList()
            } else {  // 그룹 기록
                binding.personalBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textGray
                    )
                )
                binding.groupBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                checked = true
                getList()
            }

        }
    }


    private fun dialogCreate() {

        binding.diaryMonth.setOnClickListener {

            val year = yearMonthTextView.split(".")[0]
            val month = yearMonthTextView.split(".")[1]

            YearMonthDialog(year.toInt(), month.toInt()) { selectedYearMonth ->
                yearMonthTextView = DateTime(selectedYearMonth).toString("yyyy.MM")
                binding.diaryMonth.text = yearMonthTextView
                if (yearMonthTextView != currentYearMonth) {
                    currentYearMonth = yearMonthTextView
                }
                val editor = sf.edit()
                editor.putString("yearMonth", yearMonthTextView)
                editor.apply()

                getList()
            }.show(parentFragmentManager, "test")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getPersonalList() {

        binding.diaryPersonalListRv.visibility = View.VISIBLE
        binding.diaryGroupListRv.visibility = View.GONE

        val diaryPersonalAdapter = DiaryAdapter(editClickListener = {
            onEditClickListener(it)
        }, imageClickListener = {
            ImageDialog(it).show(parentFragmentManager, "test")
        })
        binding.diaryPersonalListRv.apply {
            adapter = diaryPersonalAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val storeDB = Thread {
            val diaryItems = repo.getDiaryList(yearMonthTextView)  // 월 별 다이어리 조회

            requireActivity().runOnUiThread {

                diaryPersonalAdapter.updateData(diaryItems)

                // 달 별 메모 없으면 없다고 띄우기
                if (diaryItems.isNotEmpty()) {
                    binding.diaryPersonalListRv.visibility = View.VISIBLE
                } else {
                    binding.diaryPersonalListRv.visibility = View.GONE
                    binding.diaryListEmptyTv.visibility = View.VISIBLE
                    binding.diaryListEmptyTv.text = "메모가 없습니다. 메모를 추가해 보세요!"
                }
            }
        }

        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun getGroupList() {

        binding.diaryPersonalListRv.visibility = View.GONE
        binding.diaryGroupListRv.visibility = View.VISIBLE

        diaryGroupAdapter = DiaryGroupAdapter(imageClickListener = {
            ImageDialog(it).show(parentFragmentManager, "test")
        })
        binding.diaryGroupListRv.apply {
            adapter = diaryGroupAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        if (!NetworkManager.checkNetworkState(requireContext())) {
            //인터넷 연결 안 됨
            binding.diaryListEmptyTv.visibility = View.VISIBLE
            binding.diaryListEmptyTv.text = "네트워크 연결 실패"
            binding.diaryGroupListRv.visibility = View.GONE

            return
        }

        val yearMonthSplit = yearMonthTextView.split(".")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].removePrefix("0")
        val formatYearMonth = "$year,$month"
        val service = DiaryService()
        service.getGroupMonthDiary(formatYearMonth, 1, 7)
        service.getGroupMonthView(this)

    }

    private fun onEditClickListener(item: DiaryItem.Content) {

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


    override fun onGetGroupMonthSuccess(response: DiaryResponse.DiaryGetMonthResponse) {

        val list = arrayListOf<DiaryEvent>()
        val result = response.result.content
        result.map {
            list.add(
                DiaryEvent(
                    it.scheduleIdx,
                    it.title,
                    it.startDate,
                    it.categoryId,
                    it.placeName,
                    it.content,
                    it.imgUrl
                )
            )
        }

        // 달 별 메모 없으면 없다고 띄우기
        if (result.isNotEmpty()) {
            binding.diaryGroupListRv.visibility = View.VISIBLE
        } else {
            binding.diaryGroupListRv.visibility = View.GONE
            binding.diaryListEmptyTv.visibility = View.VISIBLE
            binding.diaryListEmptyTv.text = "메모가 없습니다. 메모를 추가해 보세요!"
        }

        val diaryItems = list.toListItems()

        diaryGroupAdapter.updateData(diaryItems)

        // 달 별 메모 없으면 없다고 띄우기
        if (diaryItems.isNotEmpty()) {
            binding.diaryGroupListRv.visibility = View.VISIBLE
        } else {
            binding.diaryGroupListRv.visibility = View.GONE
            binding.diaryListEmptyTv.visibility = View.VISIBLE
        }
    }

    override fun onGetGroupMonthFailure(message: String) {
        binding.diaryGroupListRv.visibility = View.GONE
        binding.diaryListEmptyTv.visibility = View.VISIBLE
        binding.diaryListEmptyTv.text = "네크워크 연결 성공, 서버 오류"
    }

    private fun List<DiaryEvent>.toListItems(): List<DiaryGroupItem> {
        val result = arrayListOf<DiaryGroupItem>() // 결과를 리턴할 리스트

        var groupHeaderDate: Long = 0 // 그룹날짜
        this.forEach { task ->
            // 날짜가 달라지면 그룹 헤더를 추가

            if (groupHeaderDate * 1000 != task.event_start * 1000) {
                result.add(DiaryGroupItem.Header(task.eventId, task.event_start * 1000))
            }
            //  task 추가

            result.add(
                DiaryGroupItem.Content(

                    task.eventId,
                    task.event_title,
                    task.event_start,
                    task.event_category_idx,
                    task.event_place_name,
                    task.content,
                    task.images,
                    task.eventId

                )
            )

            // 그룹 날짜를 바로 이전 날짜로 설정
            groupHeaderDate = task.event_start
        }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}