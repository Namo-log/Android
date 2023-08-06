package com.example.namo.ui.bottom.group

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.namo.data.remote.moim.AddMoimResponse
import com.example.namo.data.remote.moim.AddMoimView
import com.example.namo.data.remote.moim.MoimService
import com.example.namo.databinding.FragmentGroupCreateBinding
import com.example.namo.utils.NetworkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CreateGroupFragment : DialogFragment(), AddMoimView {

    private var _binding: FragmentGroupCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var db: NamoDatabase
    private lateinit var group: Group

    private var title: String = ""
    private var coverImage: Uri? = null
    private var member: List<String>? = null
    private var imageUri: Uri? = null
    private var imagePath : String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

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
            if (!binding.createGroupTitleEt.toString().isEmpty() && imageUri != null) {
                insertData()
                findNavController().popBackStack() //뒤로가기
                hideBottomNavigation(false)
            } else {
                Toast.makeText(context, "그룹 이미지 또는 이름을 올바르게 등록해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        //앨범 권한 확인 후 연결
        binding.createGroupCoverImgIv.setOnClickListener {
            getPermission()
        }
    }

    private fun insertData() {
        insertRoom()

        if(!NetworkManager.checkNetworkState(requireContext())) {
            //인터넷 연결 안 됨
            return
        }

        val moimService = MoimService()
        moimService.setAddMoimView(this)

        val groupNameRequestBody = binding.createGroupTitleEt.text.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())

        moimService.addMoim(getImgMultiPart(), groupNameRequestBody)
    }

    private fun insertRoom() {
        Thread {
            with(binding) {
                title = createGroupTitleEt.text.toString()
            }
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
            val intent = Intent().apply {
                type = "image/*"
                data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                action = Intent.ACTION_GET_CONTENT
            }

            getImage.launch(intent)
        }
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            if (imageUri != null) {
                imagePath = getPathFromUri(imageUri!!)
                Log.d("PATH_URI", imagePath)

                Glide.with(this)
                    .load(imageUri)
                    .fitCenter()
                    .apply(RequestOptions().override(500,500))
                    .into(binding.createGroupCoverImgIv)
            }
        }
    }

    private fun getPathFromUri(uri: Uri) : String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = mContext.contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()

        val path = cursor?.getString(columnIndex ?: -1)
        cursor?.close()

        if (path.isNullOrEmpty()) {
            return "Empty_Path"
        } else {
            return path
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }


    // API 관련
    private fun getImgMultiPart() : MultipartBody.Part {

        val file = File(imagePath)
        Log.d("CreateGroupFrag", imagePath)
        Log.d("CreateGroupFrag", file.name)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("img", file.name, requestFile)

        return imagePart
    }

    override fun onAddMoimSuccess(response: AddMoimResponse) {
        Log.d("CreateGroupFrag", "onAddMoimSuccess : Moim Id = ${response.result.moimId}")
    }

    override fun onAddMoimFailure(message: String) {
        Log.d("CreateGroupFrag", "onAddMoimFailure")

    }
}