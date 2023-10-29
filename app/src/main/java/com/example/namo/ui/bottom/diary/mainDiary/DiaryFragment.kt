package com.example.namo.ui.bottom.diary.mainDiary


import DiaryAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.data.remote.diary.DiaryService
import com.example.namo.data.remote.diary.GetGroupMonthView
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGroupAdapter
import com.example.namo.utils.NetworkManager
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.ui.bottom.home.calendar.SetMonthDialog
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import kotlin.math.roundToInt


class DiaryFragment : Fragment(), GetGroupMonthView {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis // 현재 연월 밀리초
    private var currentYearMonth: String = ""

    private lateinit var sf: SharedPreferences
    private lateinit var repo: DiaryRepository
    private lateinit var diaryGroupAdapter: DiaryGroupAdapter

    private var yearMonthTextView: String = ""
    private var checked = false
    private var service = DiaryService()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        repo = DiaryRepository(requireContext())

        yearMonthTextView = DateTime(dateTime).toString("yyyy.MM")
        sf = requireContext().getSharedPreferences("sf", Context.MODE_PRIVATE)

        val savedYearMonth = sf.getString("yearMonth", yearMonthTextView)  // 다른 화면으로 넘어가도 텍스트 유지
        if (savedYearMonth != null) {
            yearMonthTextView = savedYearMonth
        }
        val savedChecked = sf.getBoolean("checked", false)
        checked = savedChecked

        getList()

        binding.diaryMonth.text = yearMonthTextView

        checkSwitchBtn()
        dialogCreate()

        return binding.root
    }


    private fun dialogCreate() {

        binding.diaryMonthLl.setOnClickListener {

            dateTime = convertYearMonthToMillis(yearMonthTextView) // 화면에 표시된 텍스트를 밀리초로 받음
            SetMonthDialog(requireContext(), dateTime) { selectedYearMonth ->
                yearMonthTextView = DateTime(selectedYearMonth).toString("yyyy.MM")
                binding.diaryMonth.text = yearMonthTextView

                if (yearMonthTextView != currentYearMonth) {
                    currentYearMonth = yearMonthTextView
                }
                val editor = sf.edit()
                editor.putString("yearMonth", yearMonthTextView)
                editor.apply()

                getList()
            }.show()
        }
    }

    private fun convertYearMonthToMillis(
        yearMonthStr: String,
        pattern: String = "yyyy.MM"
    ): Long {  // yyyy.MM 타입을 밀리초로 변경

        val formatter = DateTimeFormat.forPattern(pattern)
        val dateTime = formatter.parseDateTime(yearMonthStr)
        return dateTime.toDate().time

    }

    private fun getList() {

        if (!checked) { // 개인 기록 가져오기
            getPersonalList()

            binding.personalBtn.setPadding(
                changeDP(0),
                binding.personalBtn.paddingTop,
                changeDP(0),
                binding.personalBtn.paddingBottom
            )
            binding.groupBtn.setPadding(
                changeDP(0),
                binding.groupBtn.paddingTop,
                changeDP(10),
                binding.groupBtn.paddingBottom
            )

            setButton(binding.personalBtn, binding.groupBtn)

        } else { // 그룹 기록 가져오기
            getGroupList()

            binding.personalBtn.setPadding(
                changeDP(10),
                binding.personalBtn.paddingTop,
                changeDP(0),
                binding.personalBtn.paddingBottom
            )
            binding.groupBtn.setPadding(
                changeDP(0),
                binding.groupBtn.paddingTop,
                changeDP(0),
                binding.groupBtn.paddingBottom
            )

            setButton(binding.groupBtn, binding.personalBtn)
        }

    }

    private fun changeDP(value: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (value * displayMetrics.density).roundToInt()
    }

    private fun checkSwitchBtn() {

        binding.personalBtn.setOnClickListener {
            checked = false
            getList()
        }

        binding.groupBtn.setOnClickListener {
            checked = true
            getList()
        }
    }

    private fun setButton(group: TextView, personal: TextView) {

        group.setBackgroundResource(R.drawable.switch_thumb)
        personal.setBackgroundResource(0)

        val layoutParams = personal.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 1f
        personal.layoutParams = layoutParams

        val groupLayoutParams = group.layoutParams as LinearLayout.LayoutParams
        groupLayoutParams.weight = 1.5f
        group.layoutParams = groupLayoutParams

        personal.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.textGray
            )
        )
        group.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
    }

    private fun getDiaryList(yearMonth: String): List<DiaryEvent> {  // 개인 다이어리 roomdb 데이터
        val db = NamoDatabase.getInstance(requireContext()).diaryDao
        return db.getDiaryEventList(yearMonth).toListItems()
    }

    private fun List<DiaryEvent>.toListItems(): List<DiaryEvent> {
        val result = mutableListOf<DiaryEvent>()
        var groupHeaderDate: Long = 0

        this.forEach { event ->
            if (groupHeaderDate * 1000 != event.event_start * 1000) {

                val headerEvent = event.copy(event_start = event.event_start * 1000,isHeader = true)
                result.add(headerEvent)

                groupHeaderDate = event.event_start
            }
            result.add(event)
        }

        return result
    }


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
            val diaryItems = getDiaryList(yearMonthTextView)

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

        diaryGroupAdapter = DiaryGroupAdapter(detailClickListener = { item ->
            onDetailClickListener(item)

        }, imageClickListener = {
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


        service.getGroupMonthDiary(formatYearMonth, 0, 7)
        service.getGroupMonthView(this)

    }

    private fun onEditClickListener(item: DiaryEvent) {  // 개인 기록 수정 클릭리스너

        val bundle = Bundle()

        val event = Event(
            item.eventId,
            item.event_title,
            item.event_start, 0L, 0,
            item.event_category_idx, item.event_place_name,
            0.0, 0.0, 0, null, 1,
            R.string.event_current_default.toString(),
            item.event_server_idx,
            item.event_category_server_idx,
            1
        )

        bundle.putSerializable("event", event)

        val detailFrag = PeraonalDetailFragment()
        detailFrag.arguments = bundle
        view?.findNavController()?.navigate(R.id.action_diaryFragment_to_diaryAddFragment, bundle)

    }

    private fun onDetailClickListener(item: DiaryEvent) {  // 그룹 기록 수정 클릭리스너

        val bundle = Bundle()
        val monthDiary = item.images?.let {
            DiaryResponse.MonthDiary(
                item.eventId, item.event_title, item.event_start, item.content,
                it, item.event_category_idx, 0L, item.event_place_name
            )
        }
        bundle.putSerializable("groupDiary", monthDiary)

        val detailFrag = GroupDetailFragment()
        detailFrag.arguments = bundle
        view?.findNavController()
            ?.navigate(R.id.action_diaryFragment_to_groupDetailFragment, bundle)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onGetGroupMonthSuccess(response: DiaryResponse.DiaryGetMonthResponse) {

        val list = arrayListOf<DiaryEvent>()
        val result = response.result.content
        result.forEach {
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
    }

    override fun onGetGroupMonthFailure(message: String) {
        binding.diaryGroupListRv.visibility = View.GONE
        binding.diaryListEmptyTv.visibility = View.VISIBLE
        binding.diaryListEmptyTv.text = "네크워크 연결 성공, 서버 오류"
    }


    override fun onDestroyView() {
        super.onDestroyView()

        val editor = sf.edit()
        editor.putBoolean("checked", checked)
        editor.apply()

        _binding = null
    }

}

