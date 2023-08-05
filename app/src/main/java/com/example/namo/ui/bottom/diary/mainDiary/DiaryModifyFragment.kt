package com.example.namo.ui.bottom.diary.mainDiary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryModifyBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class DiaryModifyFragment : Fragment(), DiaryRepository.DiaryModifyCallback {  // 다이어리 편집 화면

    private var _binding: FragmentDiaryModifyBinding? = null
    private val binding get() = _binding!!

    private var imgList: ArrayList<String?> = arrayListOf()
    private lateinit var galleryAdapter: GalleryListAdapter
    private lateinit var repo: DiaryRepository

    private lateinit var event: Event
    private lateinit var category: Category

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryModifyBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        event = (arguments?.getSerializable("event") as? Event)!!

        repo = DiaryRepository(requireContext())
        repo.setCallBack2(this)
        repo.setDiary(event.eventId, event.serverIdx)

        galleryAdapter = GalleryListAdapter(requireContext())
        bind()

        return binding.root
    }

    override fun onGetDiary(diary: Diary) {

        binding.diaryContentsEt.setText(diary.content)

        imgList.addAll(diary.images as List<String?>)
        viewImages()

        onClickListener(diary)
        onRecyclerView()
        charCnt()
    }

    @SuppressLint("SimpleDateFormat")
    private fun bind() {

        CoroutineScope(Dispatchers.Main).launch {

            val categoryIdx = if (event.categoryServerIdx == 0L) event.categoryIdx else event.categoryServerIdx
            category = repo.getCategoryId(categoryIdx)

            context?.resources?.let {
                binding.itemDiaryCategoryColorIv.background.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        category.color
                    )
                )
            }
        }

        binding.apply {
            val formatDate = SimpleDateFormat("yyyy.MM.dd (EE)").format(event.startLong)

            diaryInputDateTv.text = formatDate
            diaryInputPlaceTv.text = event.placeName
            diaryTitleTv.text = event.title
            diaryTitleTv.isSelected = true  // marquee

            diaryTodayDayTv.text = SimpleDateFormat("EE").format(event.startLong)
            diaryTodayNumTv.text = SimpleDateFormat("dd").format(event.startLong)
        }
    }

    private fun onClickListener(diary: Diary) {

        binding.diaryEditTv.setOnClickListener {
            if (binding.diaryEditTv.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "메모를 입력해주세용", Toast.LENGTH_SHORT).show()
            } else {
                updateDiary(diary)
                findNavController().popBackStack()
                hideBottomNavigation(false)

            }
        }

        binding.diaryBackIv.setOnClickListener {
            findNavController().popBackStack()
            hideBottomNavigation(false)
        }

        binding.diaryDeleteIv.setOnClickListener {
            deleteDiary()
            view?.findNavController()?.navigate(R.id.diaryFragment)
            hideBottomNavigation(false)
        }

        binding.diaryGalleryClickIv.setOnClickListener {
            getPermission()
        }
    }


    /** 다이어리 수정 **/
    private fun updateDiary(diary: Diary) {
        diary.content = binding.diaryContentsEt.text.toString()

        if (imgList.isEmpty()) diary.images = diary.images
        else diary.images = imgList as ArrayList<String>

        repo.editDiary(
            event.eventId,
            binding.diaryContentsEt.text.toString(),
            diary.images,
            event.serverIdx
        )

        Toast.makeText(requireContext(), "수정되었습니다", Toast.LENGTH_SHORT).show()
    }

    /** 다이어리 삭제 **/
    private fun deleteDiary() {
        repo.deleteDiary(event.eventId, event.serverIdx)
    }

    private fun onRecyclerView() {

        val galleryViewRVAdapter = galleryAdapter
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun viewImages() {

        galleryAdapter.addImages(imgList)
        galleryAdapter.notifyDataSetChanged()

    }

    @SuppressLint("IntentReset")
    private fun getPermission() {

        val writePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                200
            )
        } else {
            // 권한 있음
            val intent = Intent().apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }

            intent.type = MediaStore.Images.Media.CONTENT_TYPE
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
            imgList.clear()

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
            } else { // 단일 선택
                result.data?.data?.let {
                    val imageUri: Uri? = result.data!!.data
                    if (imageUri != null) {
                        imgList.add(imageUri.toString())
                    }
                }
            }

            viewImages()
        }
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