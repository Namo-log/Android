package com.mongmong.namo.presentation.ui.bottom.diary.moimDiary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.remote.diary.*
import com.mongmong.namo.databinding.ActivityMoimMemoDetailBinding
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.ui.bottom.diary.personalDiary.adapter.GalleryListAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

@AndroidEntryPoint
class MoimMemoDetailActivity: AppCompatActivity(),
    ConfirmDialogInterface {   // 그룹 기록에 대한 텍스트 추가, 삭제

    private lateinit var binding: ActivityMoimMemoDetailBinding

    private lateinit var moimSchedule: MoimDiary
    private lateinit var placeIntList: List<Long>

    private var diaryService = DiaryService()
    private var isDelete: Boolean = false
    private val viewModel : MoimDiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMoimMemoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moimSchedule  = (intent.getSerializableExtra("groupDiary") as MoimDiary)
        diaryService = DiaryService()

        initView()
        initClickListener()
        initObserve()
        charCnt()


    }

    override fun onResume() {
        super.onResume()
        viewModel.getMoimDiary(moimSchedule.scheduleId)
        binding.diaryContentsEt.setText(viewModel.getMemo())
    }

    override fun onPause() {
        super.onPause()
        viewModel.setMemo(binding.diaryContentsEt.text.toString())
    }

    private fun initView() {
        viewModel.setMemo(moimSchedule.content ?: "")
        with(binding) {
            val repo = DiaryRepository(this@MoimMemoDetailActivity)
            val category = repo.getCategory(moimSchedule.categoryId, moimSchedule.categoryId)
            itemDiaryCategoryColorIv.background.setTint(category.color)
            val scheduleDate = moimSchedule.startDate * 1000

            diaryTodayDayTv.text = DateTime(scheduleDate).toString("EE")
            diaryTodayNumTv.text = DateTime(scheduleDate).toString("dd")
            diaryTitleTv.text = moimSchedule.title
            diaryInputDateTv.text = DateTime(scheduleDate).toString("yyyy.MM.dd (EE)")
            diaryInputPlaceTv.text = moimSchedule.placeName

            diaryEditBtnTv.apply {
                if (moimSchedule.content.isNullOrEmpty()) { // 그룹 기록 내용이 없으면, 기록 저장
                    text = resources.getString(R.string.diary_add)
                    setTextColor(getColor(R.color.white))
                    setBackgroundResource(R.color.MainOrange)
                } else {  // 내용이 있으면, 기록 수정
                    text = resources.getString(R.string.diary_edit)
                    setTextColor(getColor(R.color.MainOrange))
                    setBackgroundResource(R.color.white)
                }
            }
        }
    }
    private fun initClickListener() {
        with(binding) {
            diaryDeleteIv.setOnClickListener { showDeleteDialog() } // 삭제
            diaryBackIv.setOnClickListener { finish() } // 뒤로 가기
            groupDiaryDetailLy.setOnClickListener {// 그룹 다이어리 장소 아이템 추가 화면으로 이동
                startActivity(
                    Intent(this@MoimMemoDetailActivity, MoimDiaryActivity::class.java)
                        .putExtra("hasGroupPlace", true)
                        .putExtra("groupScheduleId", moimSchedule.scheduleId)
                )
            }
            diaryEditBtnTv.setOnClickListener {
                viewModel.patchMoimMemo(
                    moimSchedule.scheduleId,
                    binding.diaryContentsEt.text.toString()
                )
                /*diaryService.addGroupAfterDiary(
                    moimSchedule.scheduleId,
                    binding.diaryContentsEt.text.toString()
                )*/
            }
        }
    }
    private fun initObserve() {
        // 모임 기록 가져오기
        viewModel.getMoimDiaryResult.observe(this) { result ->
            placeIntList = result.moimActivities.map {
                it.moimActivityId // 그룹 스케줄 별 장소 아이디 가져와서 리스트 만들기
            }
            val imgList = result.moimActivities.flatMap { it.imgs?.take(3) ?: emptyList() }

            setImgList(imgList)
        }

        // 모임 기록 메모 추가/수정
        viewModel.patchMemoResult.observe(this) { isSuccess ->
            if(isSuccess) finish()
        }
    }

    private fun setImgList(imgList: List<String>) {
        val galleryViewRVAdapter = GalleryListAdapter(this)
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        galleryViewRVAdapter.addImages(imgList)
    }

    private fun showDeleteDialog() {
        // 삭제 확인 다이얼로그
        val title = "모임 기록을 정말 삭제하시겠어요?"
        val content = "삭제한 모든 모임 기록은\n개인 기록 페이지에서도 삭제됩니다."

        val dialog = ConfirmDialog(this, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ConfirmDialog")
    }

    override fun onClickYesButton(id: Int) {
        isDelete = true
        //viewModel.patchMoimDiary(moimSchedule.scheduleId, "")
        //diaryService.addGroupAfterDiary(moimSchedule.scheduleId, "")
        //diaryService.addGroupAfterDiary(this)
    }

    suspend fun patchSuccess(response: DiaryResponse) {
        if (isDelete) {
            withContext(Dispatchers.IO) {
                placeIntList.map { placeIndex ->
                    diaryService.deleteGroupDiary(placeIndex, object : DiaryBasicView {
                        override fun onSuccess(response: DiaryResponse) {
                            Log.e("DELETE_GROUP_DIARY", "SUCCESS")
                        }

                        override fun onFailure(message: String) {
                            Log.e("DELETE_GROUP_DIARY", message)
                        }
                    })
                }
                finish()
                isDelete = false
            }
        } else {
            finish()
        }
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
                            this@MoimMemoDetailActivity, "최대 200자까지 입력 가능합니다",
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

}