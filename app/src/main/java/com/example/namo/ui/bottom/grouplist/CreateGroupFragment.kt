package com.example.namo.ui.bottom.grouplist

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.group.Group
import com.example.namo.databinding.FragmentGroupCreateBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateGroupFragment : DialogFragment() {

    private var _binding: FragmentGroupCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: NamoDatabase
    private lateinit var group: Group

    private var title: String = ""
    private var coverImage: Uri? = null
    private var member: List<String>? = null
    private var imageUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGroupCreateBinding.inflate(inflater, container, false)

        //hideBottomNavigation(true)

        // 레이아웃 배경을 투명하게 해줌
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        db= NamoDatabase.getInstance(requireContext())

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        onClickListener() //클릭 동작
    }

    private fun onClickListener() {
        // 닫기
        binding.createGroupBackTv.setOnClickListener {
            findNavController().popBackStack() //뒤로가기
            hideBottomNavigation(false)
        }
        // 확인
        binding.createGroupSaveTv.setOnClickListener {
            if (!binding.createGroupTitleEt.toString().isEmpty()) {
                insertData()
                findNavController().popBackStack() //뒤로가기
                hideBottomNavigation(false)
            }
        }

        //앨범 권한 확인 후 연결
        binding.createGroupCoverImgIv.setOnClickListener {
            getPermission()
        }
    }

    private fun insertData() {
        Thread {
            with(binding) {
                title = createGroupTitleEt.text.toString()
                coverImage = imageUri
            }
            //TODO: 그룹 생성에서 멤버 비워두기. 일단 오류 나서 임시 멤버
            member = listOf("지니", "앨리", "코코아")
            //TODO: 그룹 프로필 추가
            group = Group(0, title)
            db.groupDao.insertGroup(group)
        }.start()
    }

    private fun hideBottomNavigation( bool : Boolean){
        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
        if(bool) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
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

            data?.data?.let {
                imageUri = data.data
                if (imageUri != null) {

                    // 선택한 그룹 커버 이미지 불러오기
                    Glide.with(this)
                            .load(imageUri)
                            .fitCenter()
                            .apply(RequestOptions().override(500,500))
                            .into(binding.createGroupCoverImgIv)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }
}