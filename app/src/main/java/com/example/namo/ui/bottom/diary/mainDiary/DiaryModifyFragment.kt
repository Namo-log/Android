package com.example.namo.ui.bottom.diary.mainDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryModifyBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Boolean.FALSE
import java.text.SimpleDateFormat

class DiaryModifyFragment : Fragment(), DiaryDetailView, GetDayDiaryView {  // 다이어리 편집 화면

    private var _binding: FragmentDiaryModifyBinding? = null
    private val binding get() = _binding!!

    private var imgList = arrayListOf<String>()
    private lateinit var galleryAdapter: GalleryListAdapter
    private lateinit var repo: DiaryRepository

    private lateinit var event: Event
    private var scheduleIdx: Int = 0
    private lateinit var category: Category

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryModifyBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        val diaryDao = NamoDatabase.getInstance(requireContext()).diaryDao
        val categoryDao = NamoDatabase.getInstance(requireContext()).categoryDao
        val diaryService = DiaryService()

        repo = DiaryRepository(diaryDao, categoryDao, diaryService,requireContext())

        scheduleIdx = arguments?.getInt("scheduleIdx")!!

        // 임시 scheduleIdx
        repo.getDayDiaryRetrofit(scheduleIdx)
        diaryService.setDiaryView(this)
        diaryService.getDayDiaryView(this)

        charCnt()

        Thread {
            event = repo.getDayDiaryLocal(scheduleIdx)
            category = repo.getCategoryIdLocal(scheduleIdx)
            requireActivity().runOnUiThread {
                galleryAdapter = GalleryListAdapter(requireContext())
                event.imgs?.let { galleryAdapter.addImages(it) }
                bind()

                Log.d("s",event.toString())
            }
        }.start()

        return binding.root
    }

    override fun onEditDiarySuccess(code: Int, message: String, result: String) {
        when(code){
            1000-> {
                Log.d("onEditDiary","success")

            }
        }
        Log.d("onEditDiary","$code $message $result")
    }

    override fun onDeleteDiarySuccess(code: Int, message: String, result: String) {
        when(code){
            1000-> {
                Log.d("onDeleteDiary","success")

            }
        }
        Log.d("onDeleteDiary","$code $message $result")
    }

    override fun onGetDayDiarySuccess(
        code: Int,
        message: String,
        result: DiaryResponse.DayDiaryDto
    ) {
        when(code){
            1000-> {
                Log.d("onGetDayDiary","success")

            }
        }
        Log.d("onGetDayDiary","$code $message $result")
    }

    @SuppressLint("SimpleDateFormat")
    private fun bind() {


        binding.apply {
            val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(event.startLong)
            diaryInputDateTv.text = formatDate
            diaryInputPlaceTv.text = event.placeName
            diaryTitleTv.text = event.title
            diaryTitleTv.isSelected = true  // marquee
            diaryContentsEt.setText(event.content)
            context?.resources?.let {
                itemDiaryCategoryColorIv.background.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        category.color
                    )
                )
            }

            diaryTodayDayTv.text = SimpleDateFormat("EE").format(event.startLong)
            diaryTodayNumTv.text = SimpleDateFormat("dd").format(event.startLong)

            onRecyclerView()

            diaryEditTv.setOnClickListener {
                if (diaryEditTv.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "메모를 입력해주세용", Toast.LENGTH_SHORT).show()
                } else {
                    updateDiary()
                    view?.findNavController()?.navigate(R.id.diaryFragment)
                    hideBottomNavigation(false)
                }
            }

            diaryBackIv.setOnClickListener {
                findNavController().popBackStack()
                hideBottomNavigation(false)
            }

            binding.diaryDeleteIv.setOnClickListener {
                deleteDiary()
                view?.findNavController()?.navigate(R.id.diaryFragment)
                hideBottomNavigation(false)
            }

            diaryGalleryClickIv.setOnClickListener {
                getPermission()
            }
        }
    }

    /** 다이어리 수정 **/
    private fun updateDiary() {
        Thread {
            event.content = binding.diaryContentsEt.text.toString()

            if (imgList.isEmpty()) event.imgs = event.imgs
            else event.imgs = imgList

            event.imgs?.let {
                repo.editDiaryLocal(
                    scheduleIdx, binding.diaryContentsEt.text.toString(),
                    it
                )
            }

            // 임시 scheduleIdx
            event.imgs?.let {
                repo.editDiaryRetrofit(scheduleIdx,binding.diaryContentsEt.text.toString(),
                    it
                )
            }

        }.start()
        Toast.makeText(requireContext(), "수정되었습니다", Toast.LENGTH_SHORT).show()
    }

    /** 다이어리 삭제 **/
    private fun deleteDiary() {
        Thread {
            repo.deleteDiaryLocal(scheduleIdx, FALSE, "", listOf())

            // 임시 scheduleIdx
            repo.deleteDiaryRetrofit(scheduleIdx)
        }.start()
        Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("IntentReset")
    private fun getPermission() {

        val writePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                200
            )
        } else {
            // 권한 있음
            val intent = Intent().apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기
            intent.action = Intent.ACTION_GET_CONTENT

            getImage.launch(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(requireContext(), "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                        .show()
                    binding.diaryGalleryClickIv.visibility = View.VISIBLE
                } else {
                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri
                        imgList.add(imageUri.toString())
                    }
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
        galleryAdapter.addImages(imgList)
        galleryAdapter.notifyDataSetChanged()
    }

    private fun onRecyclerView() {

        val galleryViewRVAdapter = galleryAdapter
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun charCnt() {
        with(binding) {
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

        _binding = null
        hideBottomNavigation(false)
    }


}