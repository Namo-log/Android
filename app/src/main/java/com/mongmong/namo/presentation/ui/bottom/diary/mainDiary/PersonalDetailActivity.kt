package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.data.remote.diary.DiaryRepository
import com.mongmong.namo.databinding.ActivityPersonalDiaryDetailBinding
import com.mongmong.namo.presentation.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.presentation.utils.ImageConverter.imageToFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

@AndroidEntryPoint
class PersonalDetailActivity : AppCompatActivity(), ConfirmDialogInterface {  // 개인 다이어리 추가,수정,삭제 화면

    private lateinit var binding: ActivityPersonalDiaryDetailBinding
    private lateinit var galleryAdapter: GalleryListAdapter

    private lateinit var repo: DiaryRepository
    private var imgList: ArrayList<String?> = arrayListOf()

    private lateinit var event: Event
    private lateinit var diary: Diary

    private val viewModel : DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        galleryAdapter = GalleryListAdapter(this)

        repo = DiaryRepository(this)

        setEvent()
        charCnt()
        onClickListener()
        initObserve()
    }

    private fun setEvent() {
        event = (intent.getSerializableExtra("event") as? Event)!!
        hasDiary()

        val category = repo.getCategory(event.categoryIdx, event.categoryServerIdx)
        binding.itemDiaryCategoryColorIv.background.setTint(category.color)

        binding.apply {
            val formatDate = DateTime(event.startLong * 1000).toString("yyyy.MM.dd (EE)")
            diaryTodayDayTv.text = DateTime(event.startLong * 1000).toString("EE")
            diaryTodayNumTv.text = DateTime(event.startLong * 1000).toString("dd")
            diaryTitleTv.isSelected = true  // marquee
            diaryTitleTv.text = event.title

            if (event.placeName.isEmpty()) diaryInputPlaceTv.text = "장소 없음"
            else diaryInputPlaceTv.text = event.placeName

            diaryInputDateTv.text = formatDate
        }
    }

    private fun getDiary() {
        //diary = repo.getDiary(event.eventId) // 개별 다이어리 조회
        viewModel.getDiary(event.eventId)
    }

    private fun hasDiary() {
        if (event.hasDiary == 0) {  // 기록 없을 때, 추가

            binding.diaryEditTv.text = resources.getString(R.string.diary_add)
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.MainOrange)
            binding.diaryDeleteIv.visibility = View.GONE

            binding.diaryEditTv.setOnClickListener {
                lifecycleScope.launch {
                    insertData()
                }
            }

        } else {  // 기록 있을 떄, 수정

            getDiary()
            binding.diaryEditTv.text = resources.getString(R.string.diary_edit)
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(this, R.color.MainOrange)
            )
            binding.diaryEditTv.setBackgroundResource(R.color.white)
            binding.diaryDeleteIv.visibility = View.VISIBLE

            binding.diaryDeleteIv.setOnClickListener {
                showDialog()
            }

            binding.diaryEditTv.setOnClickListener {
                lifecycleScope.launch {
                    updateDiary()
                }
            }
        }

    }


    /** 다이어리 추가 **/
    private suspend fun insertData() {
        val content = binding.diaryContentsEt.text.toString()
        if (content.isEmpty() && imgList.isEmpty()) {
            Snackbar.make(binding.root, "내용이나 이미지를 추가해주세요!", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            diary = Diary(
                event.eventId,
                event.serverIdx,
                content,
                imgList as List<String>,
                R.string.event_current_added.toString()
            )
            viewModel.addDiary(diary, imageToFile(imgList as List<String>?, this@PersonalDetailActivity))

            //repo.addDiary(event.eventId, content, imgList as List<String>?, event.serverIdx)
            finish()
        }
    }

    /** 다이어리 수정 **/
    private suspend fun updateDiary() {
        diary.apply {
            content = binding.diaryContentsEt.text.toString()
            images = imgList as List<String>?
            state = R.string.event_current_edited.toString()
        }
        viewModel.editDiary(diary, imageToFile(imgList as List<String>?, this@PersonalDetailActivity))

        /*repo.editDiary(
            event.eventId,
            binding.diaryContentsEt.text.toString(),
            imgList as List<String>?,
            event.serverIdx
        )*/


        Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun initObserve() {
        viewModel.getDiaryResult.observe(this) { diaryResult ->
            diary = diaryResult

            diary.images?.let {
                galleryAdapter.addImages(it)
            }

            imgList.addAll(diary.images as List<String?>)

            binding.diaryContentsEt.setText(diary.content)
        }
    }
    private fun showDialog() {
        // 삭제 확인 다이얼로그
        val title = "가록을 정말 삭제하시겠습니까?"

        val dialog = ConfirmDialog(this, title, null, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    /** 다이어리 삭제 **/
    private fun deleteDiary() {
        lifecycleScope.launch {
            repo.deleteDiary(event.eventId, event.serverIdx)
        }

        Toast.makeText(this, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onClickListener() {

        binding.apply {

            diaryBackIv.setOnClickListener {
                finish()
            }

            diaryGalleryClickIv.setOnClickListener {
                getGallery()
            }
            onRecyclerView()
        }
    }

    private fun onRecyclerView() {

        val galleryViewRVAdapter = galleryAdapter
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun hasImagePermission(): Boolean { // 갤러리 권한 여부
        val writePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("IntentReset")
    private fun getGallery() {

        if (hasImagePermission()) {  // 권한 있으면 갤러리 불러오기

            val intent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }

            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기

            getImage.launch(intent)


        } else {  // 없으면 권한 받기
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ),
                200
            )
        }
    }


    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            imgList.clear()
            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(this, "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                        .show()

                    return@registerForActivityResult
                } else {
                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri
                        imgList.add(imageUri.toString())
                    }
                }
            } else { // 단일 선택
                result.data?.data?.let {
                    val imageUri: Uri? = result.data!!.data
                    if (imageUri != null) {
                        imgList.add(imageUri.toString())
                    }
                }
            }
        }
        galleryAdapter.addImages(imgList)
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
                            "최대 200자까지 입력 가능합니다 ",
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {  // editText 외 터치 시 키보드 내려감
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

}

