package com.example.namo.ui.bottom.diary.mainDiary


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryGroupDetailBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.example.namo.utils.ConfirmDialog
import com.example.namo.utils.ConfirmDialogInterface
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.joda.time.DateTime

class GroupDetailFragment : Fragment(), GetGroupDiaryView,
    AddGroupAfterDiaryView,
    ConfirmDialogInterface {  // 그룹 기록에 대한 텍스트 추가, 삭제

    private var _binding: FragmentDiaryGroupDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupSchedule: DiaryResponse.MonthDiary
    private lateinit var placeIntList: List<Long>
    private lateinit var sf: SharedPreferences

    private var diaryService = DiaryService()
    private var placeSize: Int = 0
    private var isDelete: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupDetailBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        groupSchedule = requireArguments().getSerializable("groupDiary") as DiaryResponse.MonthDiary
        diaryService = DiaryService()
        sf = requireContext().getSharedPreferences("sf", Context.MODE_PRIVATE)

        charCnt()
        editMemo()

        val getText =
            if (groupSchedule.content.isNullOrEmpty()) "" else groupSchedule.content.toString()
        saveEditText(getText)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        diaryService.getGroupDiary(groupSchedule.scheduleIdx)
        diaryService.getGroupDiaryView(this)
    }

    override fun onPause() {
        super.onPause()

        saveEditText(binding.diaryContentsEt.text.toString())
    }

    private fun saveEditText(input: String) {  // 입력한 텍스트 유지
        val editor = sf.edit()
        editor.putString("edittext", input)
        editor.apply()
    }


    private fun bind(imgList: List<String>) {

        binding.diaryBackIv.setOnClickListener {
            findNavController().popBackStack()
            hideBottomNavigation(false)
        }

        binding.groupDiaryDetailLy.setOnClickListener {// 그룹 다이어리 장소 아이템 추가 화면으로 이동

            val bundle = Bundle()
            bundle.putBoolean("hasGroupPlace", true)
            bundle.putLong("groupScheduleId", groupSchedule.scheduleIdx)
            findNavController().navigate(
                R.id.action_groupDetailFragment_to_groupMemoActivity,
                bundle
            )
        }

        val repo = DiaryRepository(requireContext())
        val category = repo.getCategory(groupSchedule.categoryId, groupSchedule.categoryId)

        context?.resources?.let {
            binding.itemDiaryCategoryColorIv.background.setTint(category.color)
        }

        val scheduleDate = groupSchedule.startDate * 1000

        binding.diaryTodayDayTv.text = DateTime(scheduleDate).toString("EE")
        binding.diaryTodayNumTv.text = DateTime(scheduleDate).toString("dd")
        binding.diaryTitleTv.text = groupSchedule.title
        binding.diaryInputDateTv.text = DateTime(scheduleDate).toString("yyyy.MM.dd (EE)")
        binding.diaryInputPlaceTv.text = groupSchedule.placeName

        val galleryViewRVAdapter = GalleryListAdapter(requireContext())
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        galleryViewRVAdapter.addImages(imgList)

        binding.diaryContentsEt.setText(groupSchedule.content)
    }

    private fun editMemo() {

        binding.diaryEditTv.setOnClickListener {

            diaryService.addGroupAfterDiary(
                groupSchedule.scheduleIdx,
                binding.diaryContentsEt.text.toString()
            )
            diaryService.addGroupAfterDiary(this)
        }

        if (groupSchedule.content.isNullOrEmpty()) {  // 그룹 기록 내용이 없으면, 기록 저장
            binding.diaryEditTv.text = resources.getString(R.string.diary_add)
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.MainOrange)

        } else {  // 내용이 있으면, 기록 수정
            binding.diaryEditTv.text = resources.getString(R.string.diary_edit)
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.MainOrange
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.white)
        }
    }

    override fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse) {
        Log.d("GET_GROUP_DIARY", response.toString())

        val result = response.result
        placeIntList = result.locationDtos.map {
            it.moimMemoLocationId // 그룹 스케줄 별 장소 아이디 가져와서 리스트 만들기
        }
        placeSize = placeIntList.size
        val imgList = result.locationDtos.flatMap { it.imgs.take(3) }

        bind(imgList)
        deletePlace()

        val getText = sf.getString("edittext", "")
        binding.diaryContentsEt.setText(getText.toString())

    }

    override fun onGetGroupDiaryFailure(message: String) {
        Log.e("GET_GROUP_DIARY", message)
    }

    private fun deletePlace() {  // 장소 전체 삭제 버튼
        binding.diaryDeleteIv.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        // 삭제 확인 다이얼로그
        val title = "모임 기록을 정말 삭제하시겠어요?"
        val content = "삭제한 모든 모임 기록은\n개인 기록 페이지에서도 삭제됩니다."

        val dialog = ConfirmDialog(this, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ConfirmDialog")
    }

    override fun onClickYesButton(id: Int) {
        isDelete = true
        diaryService.addGroupAfterDiary(groupSchedule.scheduleIdx, "")
        diaryService.addGroupAfterDiary(this)
    }

    override fun onAddGroupAfterDiarySuccess(response: DiaryResponse.DiaryResponse) {

        if (isDelete) {
            CoroutineScope(Dispatchers.Main).launch {
                placeIntList.map { placeIndex ->
                    withContext(Dispatchers.IO) {
                        diaryService.deleteGroupDiary(placeIndex, object : DiaryBasicView {
                            override fun onSuccess(response: DiaryResponse.DiaryResponse) {
                                Log.e("DELETE_GROUP_DIARY", "SUCCESS")
                                placeSize--
                                if (placeSize == 0) {
                                    findNavController().popBackStack()
                                    isDelete = false
                                }
                            }

                            override fun onFailure(message: String) {
                                Log.e("DELETE_GROUP_DIARY", message)
                                findNavController().popBackStack()
                            }

                        })
                    }
                }
            }
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onAddGroupAfterDiaryFailure(message: String) {
        findNavController().popBackStack()
    }


    /** 글자 수 반환 **/
    @SuppressLint("SetTextI18n")
    private fun charCnt() {
        with(binding) {
            textNumTv.text = "${binding.diaryContentsEt.text.length} / 200"
            diaryContentsEt.addTextChangedListener(object : TextWatcher {
                var maxText = ""
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    maxText = s.toString()
                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (diaryContentsEt.length() > 200) {
                        Toast.makeText(
                            requireContext(), "최대 200자까지 입력 가능합니다",
                            Toast.LENGTH_SHORT
                        ).show()

                        diaryContentsEt.setText(maxText)
                        diaryContentsEt.setSelection(diaryContentsEt.length())
                        if (s != null) {
                            textNumTv.text = "${s.length} / 200"
                        }
                    } else {
                        textNumTv.text = "${s.toString().length} / 200"
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
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

        val editor = sf.edit()
        editor.clear()
        editor.apply()

        _binding = null
        hideBottomNavigation(false)
    }
}