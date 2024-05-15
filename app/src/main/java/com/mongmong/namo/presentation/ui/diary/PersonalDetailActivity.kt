package com.mongmong.namo.presentation.ui.diary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.ActivityPersonalDiaryDetailBinding
import com.mongmong.namo.presentation.ui.diary.adapter.GalleryListAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.PermissionChecker.hasImagePermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Locale

@AndroidEntryPoint
class PersonalDetailActivity : AppCompatActivity(), ConfirmDialogInterface {  // 개인 다이어리 추가,수정,삭제 화면
    private lateinit var binding: ActivityPersonalDiaryDetailBinding
    private lateinit var galleryAdapter: GalleryListAdapter

    private lateinit var schedule: Schedule

    private val viewModel : DiaryDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        galleryAdapter = GalleryListAdapter(this)

        setSchedule()
        charCnt()
        onClickListener()
        initRecyclerView()
        initObserve()
    }

    private fun setSchedule() {
        schedule = (intent.getSerializableExtra("schedule") as? Schedule)!!
        hasDiary()

        findCategory(schedule)

        binding.apply {
            val formatDate = DateTime(schedule.startLong * 1000).toString("yyyy.MM.dd (EE)")
            diaryTodayMonthTv.text = DateTime(schedule.startLong * 1000).toString("MMM", Locale.ENGLISH)
            diaryTodayNumTv.text = DateTime(schedule.startLong * 1000).toString("dd")
            diaryTitleTv.isSelected = true  // marquee
            diaryTitleTv.text = schedule.title

            if (schedule.placeName.isEmpty()) diaryInputPlaceTv.text = NO_PLACE
            else diaryInputPlaceTv.text = schedule.placeName

            diaryInputDateTv.text = formatDate
        }
    }

    private fun hasDiary() {
        if (schedule.hasDiary == false) {  // 기록 없을 때, 추가
            viewModel.setNewPersonalDiary(schedule, "")
            binding.diaryEditBtnTv.apply {
                text = resources.getString(R.string.diary_add)
                setTextColor(getColor(R.color.white))
                setBackgroundResource(R.color.MainOrange)
                elevation = 0f
            }
            binding.diaryDeleteIv.visibility = View.GONE
        } else {  // 기록 있을 때, 수정
            viewModel.getExistingPersonalDiary(schedule.scheduleId)
            binding.diaryEditBtnTv.apply {
                text = resources.getString(R.string.diary_edit)
                setTextColor(getColor(R.color.MainOrange))
                setBackgroundResource(R.color.white)
                elevation = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10f,
                    resources.displayMetrics
                )
            }
            binding.diaryDeleteIv.visibility = View.VISIBLE
        }
    }

    private fun findCategory(schedule: Schedule) {
        lifecycleScope.launch {
            viewModel.findCategoryById(schedule.categoryId, schedule.categoryServerId)
        }
    }

    private fun onClickListener() {
        binding.apply {
            diaryBackIv.setOnClickListener { finish() }
            diaryGalleryClickIv.setOnClickListener { getGallery() }
            diaryEditBtnTv.setOnClickListener {
                lifecycleScope.launch {
                    if(schedule.hasDiary == false) insertData()
                    else updateDiary()
                }
            }
            diaryDeleteIv.setOnClickListener {
                showDialog()
            }
        }
    }

    private fun initRecyclerView() {
        val galleryViewRVAdapter = galleryAdapter
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    /** 다이어리 추가 **/
    private fun insertData() {
        val content = binding.diaryContentsEt.text.toString()
        if (content.isEmpty() && viewModel.getImgList().isNullOrEmpty()) {
            Snackbar.make(binding.root, "내용이나 이미지를 추가해주세요!", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            viewModel.setNewPersonalDiary(schedule, content)
            viewModel.addPersonalDiary()

            finish()
        }
    }

    /** 다이어리 수정 **/
    private fun updateDiary() {
        viewModel.editPersonalDiary(binding.diaryContentsEt.text.toString())

        Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** 다이어리 삭제 **/
    private fun deleteDiary() {
        viewModel.deletePersonalDiary(schedule.scheduleId, schedule.serverId)
    }

    private fun initObserve() {
        viewModel.diary.observe(this) { diary ->
            viewModel.updateImgList(diary.images?: emptyList())
            binding.diaryContentsEt.setText(diary.content)
        }
        viewModel.imgList.observe(this) {
            galleryAdapter.addImages(it)
        }
        viewModel.deleteDiaryResult.observe(this) { isComplete ->
            // 다이어리 삭제 작업이 완료되었을 때 finish() 호출
            if (isComplete) {
                Toast.makeText(this, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        viewModel.category.observe(this) {
            binding.itemDiaryCategoryColorIv.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(it.paletteId)
        }
    }

    private fun showDialog() {
        // 삭제 확인 다이얼로그
        val title = "가록을 정말 삭제하시겠습니까?"

        val dialog = ConfirmDialog(this, title, null, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    /** 갤러리에서 이미지 가져오기 **/
    private fun getGallery() {
        if (hasImagePermission(this)) {
            val galleryIntent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                type = "image/*"
                data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기
            }
            getImage.launch(galleryIntent)
        } else {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
            ActivityCompat.requestPermissions(this, permissions, 200)
        }
    }
    //
    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val imageUris = getImageUrisFromResult(result)
        if(imageUris.isNullOrEmpty()) return@registerForActivityResult
        if (imageUris.size > 3) { // 사진 3장 이상 선택 시
            Toast.makeText(this, "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                .show()
            return@registerForActivityResult
        }
        viewModel.updateImgList(imageUris)
    }
    //
    private fun getImageUrisFromResult(result: ActivityResult): List<String> {
        if (result.resultCode != Activity.RESULT_OK) return emptyList()

        return result.data?.let { data ->
            mutableListOf<String>().apply {
                // 다중 선택 처리
                data.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) { // 최대 3개
                        add(clipData.getItemAt(i).uri.toString())
                    }
                } ?: data.data?.let { uri ->
                    // 단일 선택 처리
                    add(uri.toString())
                }
            }
        } ?: emptyList() // 결과 데이터가 null인 경우 빈 리스트 반환
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
                            this@PersonalDetailActivity,
                            "최대 200자까지 입력 가능합니다",
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

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {  //
        val focusView = currentFocus
        if (focusView != null && ev != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()

            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onClickYesButton(id: Int) {
        // 삭제 버튼 누르면 삭제 진행
        deleteDiary()
    }

    companion object {
        const val NO_PLACE = "장소 없음"
    }
}

