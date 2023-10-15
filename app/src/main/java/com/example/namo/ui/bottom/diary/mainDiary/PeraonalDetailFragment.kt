package com.example.namo.ui.bottom.diary.mainDiary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryPersonalDetailBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.example.namo.utils.ConfirmDialog
import com.example.namo.utils.ConfirmDialogInterface
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import org.joda.time.DateTime

class PeraonalDetailFragment : Fragment(), ConfirmDialogInterface {  // 다이어리 추가 화면

    private var _binding: FragmentDiaryPersonalDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryAdapter: GalleryListAdapter

    private lateinit var repo: DiaryRepository
    private var imgList: ArrayList<String?> = arrayListOf()

    private lateinit var event: Event
    private lateinit var diary: Diary

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =FragmentDiaryPersonalDetailBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        galleryAdapter = GalleryListAdapter(requireContext())

        repo = DiaryRepository(requireContext())

        setEvent()
        charCnt()
        onClickListener()

        return binding.root
    }

    private fun getDiary() {
        diary = repo.getDiary(event.eventId) // 개별 다이어리 조회

        diary.images?.let {
            galleryAdapter.addImages(it) }

        imgList.addAll(diary.images as List<String?>)

        binding.diaryContentsEt.setText(diary.content)
    }

    private fun setEvent() {

        event = (arguments?.getSerializable("event") as? Event)!!
        hasDiary()

        val category = repo.getCategory(event.categoryIdx, event.categoryServerIdx)
        context?.resources?.let {
            binding.itemDiaryCategoryColorIv.background.setTint(category.color)
        }

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

    private fun hasDiary() {

        if (event.hasDiary == 0) {  // 기록 없을 때, 추가

            binding.diaryEditTv.text = resources.getString(R.string.diary_add)
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.MainOrange)
            binding.diaryDeleteIv.visibility = View.GONE

            binding.diaryEditTv.setOnClickListener {
                insertData()
            }

        } else {  // 기록 있을 떄, 수정

            getDiary()
            binding.diaryEditTv.text = resources.getString(R.string.diary_edit)
            binding.diaryEditTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.MainOrange
                )
            )
            binding.diaryEditTv.setBackgroundResource(R.color.white)
            binding.diaryDeleteIv.visibility = View.VISIBLE

            binding.diaryDeleteIv.setOnClickListener {
                showDialog()
            }

            binding.diaryEditTv.setOnClickListener {
                updateDiary()
            }
        }

    }


    /** 다이어리 추가 **/
    private fun insertData() {

        val content = binding.diaryContentsEt.text.toString()

        if (content.isEmpty() && imgList.isEmpty()) {
            Snackbar.make(binding.root, "내용이나 이미지를 추가해주세요!", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            repo.addDiary(event.eventId, content, imgList as List<String>?, event.serverIdx)
            findNavController().popBackStack()
            hideBottomNavigation(false)
        }

    }

    /** 다이어리 수정 **/
    private fun updateDiary() {
        diary.content = binding.diaryContentsEt.text.toString()

        repo.editDiary(
            event.eventId,
            binding.diaryContentsEt.text.toString(),
            imgList as List<String>?,
            event.serverIdx
        )

        Toast.makeText(requireContext(), "수정되었습니다", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
        hideBottomNavigation(false)
    }

    private fun showDialog() {
        // 삭제 확인 다이얼로그
        val title = "가록을 정말 삭제하시겠습니까?"

        val dialog = ConfirmDialog(this@PeraonalDetailFragment, title, null, "삭제", 0)
        dialog.isCancelable = false
        activity?.let {dialog.show(it.supportFragmentManager, "ConfirmDialog")}
    }

    /** 다이어리 삭제 **/
    private fun deleteDiary() {

        repo.deleteDiary(event.eventId, event.serverIdx)
        Toast.makeText(context, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
        hideBottomNavigation(false)
    }

    private fun onClickListener() {

        binding.apply {

            diaryBackIv.setOnClickListener {
                findNavController().popBackStack()
                hideBottomNavigation(false)
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
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun hasImagePermission(): Boolean { // 갤러리 권한 여부
        val writePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),
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
                requireActivity(),
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

        if (result.resultCode == RESULT_OK) {
            imgList.clear()
            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(requireContext(), "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
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

    override fun onClickYesButton(id: Int) {
        // 삭제 버튼 누르면 삭제 진행
        deleteDiary()
    }


}