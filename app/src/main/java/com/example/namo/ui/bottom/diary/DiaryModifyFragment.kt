package com.example.namo.ui.bottom.diary


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.databinding.FragmentDiaryModifyBinding
import com.example.namo.ui.bottom.diary.adapter.DiaryGalleryRVAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat

class DiaryModifyFragment : Fragment() {

    private var _binding: FragmentDiaryModifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var db:NamoDatabase
    private var imgList= arrayListOf<String>()
    private lateinit var galleryAdapter: DiaryGalleryRVAdapter
    private lateinit var diary:Diary
    private var diaryIdx:Int=0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryModifyBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        db=NamoDatabase.getInstance(requireContext())

        diaryIdx= arguments?.getInt("diaryIdx")!!

        Thread {
            diary = db.diaryDao.getDiaryDaily(diaryIdx)
            galleryAdapter=DiaryGalleryRVAdapter(requireContext(),diary.imgList)
            requireActivity().runOnUiThread {
                bind()
            }
        }.start()

        charCnt()

        return binding.root
    }

    private fun bind(){

        binding.apply {
            val formatDate=SimpleDateFormat("yyyy.MM.dd (EE)").format(diary.date)
            diaryInputDateTv.text=formatDate
            diaryInputPlaceTv.text=diary.place
            diaryTitleTv.text=diary.title
            diaryContentsEt.setText(diary.content)
            context?.resources?.let { itemDiaryCategoryColorIv.background.setTint(it.getColor(diary.categoryColor)) }

            diaryTodayDayTv.text=SimpleDateFormat("EE").format(diary.date)
            diaryTodayNumTv.text=SimpleDateFormat("dd").format(diary.date)

            onRecyclerView()

            diaryEditTv.setOnClickListener {
                if(diaryEditTv.text.toString().isEmpty()){
                    Toast.makeText(requireContext(),"메모를 입력해주세용",Toast.LENGTH_SHORT).show()
                }else {
                    updateDiary()
                    view?.findNavController()?.navigate(R.id.diaryFragment)
                    hideBottomNavigation(false)
                }
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

    private fun updateDiary(){
        Thread{
            diary.content= binding.diaryContentsEt.text.toString()
            db.diaryDao.updateDiary(diary)
        }.start()
    }

    private fun deleteDiary(){
        Thread{
            db.diaryDao.deleteDiary(diary)
        }.start()
    }

    @SuppressLint("IntentReset")
    private fun getPermission(){

        val writePermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),200)
        } else {
            // 권한 있음
            val intent = Intent()
            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ( requestCode == 200) {

            if (data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = data.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(requireContext(), "사진은 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri

                    imgList.add(imageUri.toString())
                }

            }
        } else { // 단일 선택
            data?.data?.let {
                val imageUri : Uri? = data.data
                if (imageUri != null) {

                    imgList.add(imageUri.toString())
                } } }
        onRecyclerView()
    }

    private fun onRecyclerView() {

        val galleryViewRVAdapter = galleryAdapter
        galleryAdapter=DiaryGalleryRVAdapter(requireContext(),imgList)
        binding.diaryGallerySavedRy.adapter = galleryViewRVAdapter
        binding.diaryGallerySavedRy.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun charCnt(){
        with(binding) {
            diaryContentsEt.addTextChangedListener(object : TextWatcher {
                var maxText=""
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    maxText=s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(diaryContentsEt.length() > 200){
                        Toast.makeText(requireContext(),"최대 200자까지 입력 가능합니다",
                            Toast.LENGTH_SHORT).show()

                        diaryContentsEt.setText(maxText)
                        diaryContentsEt.setSelection(diaryContentsEt.length())
                        textNumTv.text="${diaryEditTv.length()} / 200"
                    } else { textNumTv.text="${diaryEditTv.length()} / 200"}
                }
                override fun afterTextChanged(s: Editable?) {
                }

            })
        }
    }

    private fun hideBottomNavigation( bool : Boolean){
        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
        if(bool) {
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