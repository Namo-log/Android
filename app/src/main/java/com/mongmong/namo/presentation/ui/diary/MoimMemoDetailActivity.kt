package com.mongmong.namo.presentation.ui.diary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMoimMemoDetailBinding
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.ui.diary.adapter.GalleryListAdapter
import com.mongmong.namo.presentation.ui.group.diary.MoimDiaryActivity
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import java.util.Locale

@AndroidEntryPoint
class MoimMemoDetailActivity: AppCompatActivity(), ConfirmDialogInterface {
    private lateinit var binding: ActivityMoimMemoDetailBinding

    private var moimScheduleId: Long = 0L
    private val viewModel : DiaryDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMoimMemoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewModel = this@MoimMemoDetailActivity.viewModel
            lifecycleOwner = this@MoimMemoDetailActivity
        }

        moimScheduleId = intent.getLongExtra("moimScheduleId", 0L)

        initObserve()
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMoimMemo(moimScheduleId)
    }

    private fun initClickListener() {
        with(binding) {
            diaryDeleteIv.setOnClickListener { showDeleteDialog() } // 삭제
            diaryBackIv.setOnClickListener { finish() } // 뒤로 가기
            groupDiaryDetailLy.setOnClickListener {// 그룹 다이어리 장소 아이템 추가 화면으로 이동
                startActivity(
                    Intent(this@MoimMemoDetailActivity, MoimDiaryActivity::class.java)
                        .putExtra("from", "moimMemo")
                        .putExtra("hasMoimActivity", true)
                        .putExtra("moimScheduleId", moimScheduleId)
                )
            }
            diaryEditBtnTv.setOnClickListener {
                this@MoimMemoDetailActivity.viewModel.patchMoimMemo(moimScheduleId)
            }
        }
    }
    private fun initObserve() {
        // 모임 메모 가져오기
        viewModel.moimDiary.observe(this) { diary ->
            Log.d("getMoimMemoResponse", "$diary")
            diary?.let{
                initView(diary)
            }
        }
        // 모임 기록 메모 추가/수정
        viewModel.patchMemoResult.observe(this) { isSuccess ->
            if(isSuccess) finish()
        }

        // 모임 기록 메모 삭제
        viewModel.deleteMemoResult.observe(this) { isComplete ->
            Log.d("isDeleteComplete", "$isComplete")
            if(isComplete) finish()
            else Toast.makeText(this, "네트워크 오류", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView(moimDiary: MoimDiary) {
        viewModel.isEditMode()
        viewModel.findCategoryById()
        setImgList(moimDiary.urls)
    }

    private fun setImgList(imgList: List<String>) {
        val galleryViewRVAdapter = GalleryListAdapter(this)
        binding.diaryGallerySavedRv.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRv.layoutManager =
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
        viewModel.deleteMoimMemo(moimScheduleId)
    }

}