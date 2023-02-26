package com.example.namo.ui.bottom.grouplist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.FragmentGroupCreateBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateGroupFragment : DialogFragment() {

    private var _binding: FragmentGroupCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: NamoDatabase

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGroupCreateBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        // 레이아웃 배경을 투명하게 해줌
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        db= NamoDatabase.getInstance(requireContext())
        bind()

        return binding.root
    }

    private fun bind() {
        binding.apply {

            createGroupBackTv.setOnClickListener {
                findNavController().popBackStack()
                hideBottomNavigation(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        onClickListener() //클릭 동작
    }


    private fun onClickListener() {

//        binding.createGroupBackTv.setOnClickListener {
//            finish() //뒤로가기
//        }

        //앨범 권한 확인 후 연결
        binding.createGroupCoverImgIv.setOnClickListener {

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

    // 이미지 실제 경로 반환
//    fun getRealPathFromURI(uri: Uri): String {
//        val buildName = Build.MANUFACTURER
//        if (buildName.equals("Xiaomi")) {
//            return uri.path!!
//        }
//        var columnIndex = 0
//        val proj = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = contentResolver.query(uri, proj, null, null, null)
//        if (cursor!!.moveToFirst()) {
//            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//        }
//        val result = cursor.getString(columnIndex)
//        cursor.close()
//
//        return result
//    }

    // 이미지를 결과값으로 받는 변수
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        result ->
//        if(result.resultCode == RESULT_OK) {
//            // 이미지를 받으면 그룹 ImageView에 적용
//            val imageUri = result.data?.data
//            imageUri?.let {
//
//                // 선택한 그룹 커버 이미지 불러오기
//                Glide.with(this)
//                    .load(imageUri)
//                    .fitCenter()
//                    .apply(RequestOptions().override(500,500))
//                    .into(binding.createGroupCoverImgIv)
//            }
//
//        }
    }

    companion object {
        const val REVIEW_MIN_LENGET = 10
        // 갤러리 권한 요청
        const val REQ_GALLERY = 1
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }
}