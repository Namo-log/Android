package com.mongmong.namo.ui.bottom.group

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.R
import com.mongmong.namo.data.NamoDatabase
import com.mongmong.namo.data.entity.group.Group
import com.mongmong.namo.data.remote.moim.AddMoimResponse
import com.mongmong.namo.data.remote.moim.AddMoimView
import com.mongmong.namo.data.remote.moim.MoimService
import com.mongmong.namo.databinding.DialogGroupCreateBinding
import com.mongmong.namo.utils.NetworkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CreateGroupDialog : DialogFragment(), AddMoimView {

    private var _binding: DialogGroupCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var db: NamoDatabase
    private lateinit var group: Group

    private var title: String = ""
    lateinit var coverImg: MultipartBody.Part
    private var member: List<String>? = null
    private var imageUri: Uri? = null
    private var imagePath: String = ""

    // 갤러리에서 이미지를 선택하기 위한 상수
    private val REQUEST_IMAGE_PICK = 100

    private var clickable = true // 중복 생성을 방지하기 위함

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DialogGroupCreateBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        db = NamoDatabase.getInstance(requireContext())

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        onClickListener() // 클릭 동작
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }

    private fun onClickListener() {
        // 닫기
        binding.createGroupBackTv.setOnClickListener {
            dismiss()
        }
        // 확인
        binding.createGroupSaveTv.setOnClickListener {
            if (binding.createGroupTitleEt.toString().isNotEmpty() && imageUri != null) {
                if (clickable) {
                    insertData()
                }
                clickable = false
            } else {
                Toast.makeText(
                    context,
                    "그룹 이미지 또는 이름을 올바르게 등록해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 앨범 권한 확인 후 연결
        binding.createGroupCoverImgIv.setOnClickListener {
            openGallery()
        }
    }

    private fun insertData() {
        insertRoom()

        if (!NetworkManager.checkNetworkState(requireContext())) {
            // 인터넷 연결 안 됨
            return
        }

        val moimService = MoimService()
        moimService.setAddMoimView(this)

        imageUri?.let { uri ->
            val imgFile = imageToMultipart(uri)
            if (imgFile != null) {
                val groupNameRequestBody =
                    binding.createGroupTitleEt.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                moimService.addMoim(imgFile, groupNameRequestBody)
            } else {
                Toast.makeText(context, "이미지 파일을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertRoom() {
        Thread {
            with(binding) {
                title = createGroupTitleEt.text.toString()
            }
            // TODO: 그룹 프로필 추가
            group = Group(0, title)
            db.groupDao.insertGroup(group)
        }.start()
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

//    private fun getPermission() {
//        val readPermission = ContextCompat.checkSelfPermission(
//            requireContext(),
//            android.Manifest.permission.READ_EXTERNAL_STORAGE
//        )
//
//        if (readPermission == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                200
//            )
//        } else {
//            openGallery()
//        }
//    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        if (hasImagePermission()) { // 권한이 있으면 갤러리 불러오기
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
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let {
                imageUri = result.data!!.data
                if (imageUri != null) {
                    imagePath = imageUri.toString()
                    Log.d("PATH_URI", imagePath)

                    Glide.with(this)
                        .load(imageUri)
                        .fitCenter()
                        .apply(RequestOptions().override(500,500))
                        .into(binding.createGroupCoverImgIv)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            binding.createGroupCoverImgIv.setImageURI(imageUri)
        }
    }

    private fun imageToMultipart(imgUri: Uri): MultipartBody.Part? {
        val imagePath = getImagePathFromUri(imgUri)
        if (imagePath.isNullOrEmpty()) {
            return null
        }

        val file = File(imagePath)

        // FileProvider를 사용하여 파일 경로를 생성
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)

        val requestFile = requireContext().contentResolver.openInputStream(uri)?.use {
            file.asRequestBody("image/*".toMediaTypeOrNull())
        }

        return if (requestFile != null) {
            MultipartBody.Part.createFormData("img", file.name, requestFile)
        } else {
            null
        }
    }

    private fun getImagePathFromUri(uri: Uri): String? {
        val cursor = mContext.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) { // 커서에 데이터가 있는지 확인
                val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (idx != -1) { // 인덱스가 유효한지 확인
                    return it.getString(idx)
                }
            }
        }
        return null
    }

    override fun onAddMoimSuccess(response: AddMoimResponse) {
        Log.d("CreateGroupFrag", "onAddMoimSuccess : Moim Id = ${response.result.moimId}")
        Toast.makeText(requireContext(), "그룹 생성에 성공하였습니다.", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onAddMoimFailure(message: String) {
        Log.d("CreateGroupFrag", "onAddMoimFailure, $message")
    }
}