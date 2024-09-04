package com.mongmong.namo.presentation.ui.diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMoimMemoDetailBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.diary.adapter.GalleryImageRVAdapter
import com.mongmong.namo.presentation.ui.group.diary.MoimDiaryActivity
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class MoimMemoDetailActivity
    : BaseActivity<ActivityMoimMemoDetailBinding>(R.layout.activity_moim_memo_detail),
    ConfirmDialogInterface {
    private var moimScheduleId: Long = 0L
    private val viewModel : DiaryDetailViewModel by viewModels()

    override fun setup() {
        binding.apply {
            viewModel = this@MoimMemoDetailActivity.viewModel
            paletteId = intent.getIntExtra("paletteId", 0)
            // marquee focus
            diaryTitleTv.requestFocus()
            diaryTitleTv.isSelected = true
        }

        moimScheduleId = intent.getLongExtra("moimScheduleId", 0L)

        initObserve()
        onClickListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMoimMemo(moimScheduleId)
    }

    private fun onClickListener() {
        with(binding) {
            // 뒤로 가기
            onBackPressedDispatcher.addCallback(this@MoimMemoDetailActivity, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (this@MoimMemoDetailActivity.viewModel.isMoimDiaryChanged()) {
                        showBackDialog()
                    } else finish()
                }
            })
            diaryBackIv.setOnClickListener {
                if (this@MoimMemoDetailActivity.viewModel.isDiaryChanged()) {
                    showBackDialog()
                } else finish()
            }

            diaryDeleteIv.setOnClickListener { showDeleteDialog() } // 삭제

            groupDiaryDetailLy.setOnClickListener {// 모임 기록으로 이동
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
        setImgList(moimDiary.getImageUrls())
    }

    private fun setImgList(imgList: List<String>) {
        val diaryImages = imgList.map { DiaryImage(diaryImageId = 0, imageUrl = it, 0) }
        val galleryViewRVAdapter = GalleryImageRVAdapter(true, {}, {
            startActivity(
                Intent(this, DiaryImageDetailActivity::class.java).putExtra("imgs", it as ArrayList<DiaryImage>)
            )
        })
        binding.diaryGallerySavedRv.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        galleryViewRVAdapter.addImages(diaryImages)
    }

    private fun showDeleteDialog() {
        // 삭제 확인 다이얼로그
        val title = "모임 기록을 정말 삭제하시겠어요?"
        val content = "삭제한 모든 모임 기록은\n개인 기록 페이지에서도 삭제됩니다."

        val dialog = ConfirmDialog(this, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ConfirmDialog")
    }

    private fun showBackDialog() {
        val title = "편집한 내용이 저장되지 않습니다."
        val content = "정말 나가시겠어요?"

        val dialog = ConfirmDialog(this, title, content, "확인", MoimDiaryActivity.BACK_BUTTON_ACTION)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    override fun onClickYesButton(id: Int) {
        when(id) {
            PersonalDetailActivity.DELETE_BUTTON_ACTION -> viewModel.deleteMoimMemo(moimScheduleId) // 삭제
            PersonalDetailActivity.BACK_BUTTON_ACTION -> finish() // 뒤로가기
        }
    }

    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }
}
