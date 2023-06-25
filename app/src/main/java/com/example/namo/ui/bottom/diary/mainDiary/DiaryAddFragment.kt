package com.example.namo.ui.bottom.diary.mainDiary

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
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
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentDiaryAddBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Boolean.TRUE
import java.text.SimpleDateFormat

class DiaryAddFragment : Fragment() {  // 다이어리 추가 화면

    private var _binding: FragmentDiaryAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: NamoDatabase

    private lateinit var galleryAdapter: GalleryListAdapter
    private lateinit var event: Event

    private var imgList = arrayListOf<String>()
    private var scheduleIdx: Int = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryAddBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        db = NamoDatabase.getInstance(requireContext())

        galleryAdapter = GalleryListAdapter(requireContext())
        scheduleIdx = arguments?.getInt("scheduleIdx")!!

        charCnt()
        bind()
        return binding.root
    }


    @SuppressLint("SimpleDateFormat")
    private fun bind() {
        val categoryIdx = requireArguments().getInt("categoryIdx")

        val r = Runnable {
            try {
                event = db.diaryDao.getSchedule(scheduleIdx)
                val category = db.categoryDao.getCategoryContent(categoryIdx)

                requireActivity().runOnUiThread {
                    binding.apply {

                        val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(event.startLong)
                        diaryTodayDayTv.text = SimpleDateFormat("EE").format(event.startLong)
                        diaryTodayNumTv.text = SimpleDateFormat("dd").format(event.startLong)
                        diaryTitleTv.isSelected = true  // marquee
                        diaryTitleTv.text = event.title

                        if (event.place.isEmpty()) diaryInputPlaceTv.text = "장소 없음"
                        else diaryInputPlaceTv.text = event.placeName

                        context?.resources?.let {
                            itemDiaryCategoryColorIv.background.setTint(
                                ContextCompat.getColor(requireContext(), category.color)
                            )
                        }
                        diaryInputDateTv.text = formatDate
                    }
                }

            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()

        binding.apply {

            diaryBackIv.setOnClickListener {
                findNavController().popBackStack()
                hideBottomNavigation(false)
            }

            diaryEditTv.setOnClickListener {
                if (diaryEditTv.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "메모를 입력해주세용", Toast.LENGTH_SHORT).show()
                } else {
                    insertData()
                    view?.findNavController()?.navigate(R.id.homeFragment)
                    hideBottomNavigation(false)
                }
            }

            diaryGalleryClickIv.setOnClickListener {
                getGallery()
            }
            onRecyclerView()
        }
    }

    /** 다이어리 추가 **/
    private fun insertData() {
        Thread {
            db.diaryDao.addDiary(
                scheduleIdx,
                TRUE,
                binding.diaryContentsEt.text.toString(),
                imgList
            )
        }.start()
    }


    private fun hasImagePermission(): Boolean { // 갤러리 권한 여부
        val writePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("IntentReset")
    private fun getGallery() {

        if (hasImagePermission()) {  // 권한 있으면 갤러리 불러오기

            val intent = Intent().apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기
            intent.action = Intent.ACTION_GET_CONTENT

            getImage.launch(intent)

            binding.diaryGalleryClickIv.visibility = View.GONE
            binding.diaryGallerySavedRy.visibility = View.VISIBLE

        } else {  // 없으면 권한 받기
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                200
            )

            binding.diaryGalleryClickIv.visibility = View.VISIBLE
        }
    }


    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {

            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(requireContext(), "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                        .show()
                    binding.diaryGalleryClickIv.visibility = View.VISIBLE
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

    private fun onRecyclerView() {

        val galleryViewRVAdapter = galleryAdapter
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    /** 글자 수 반환 **/
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