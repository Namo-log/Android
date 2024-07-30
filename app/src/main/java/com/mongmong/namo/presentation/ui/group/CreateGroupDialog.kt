package com.mongmong.namo.presentation.ui.group

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.DialogGroupCreateBinding
import com.mongmong.namo.presentation.utils.NetworkCheckerImpl
import com.mongmong.namo.presentation.utils.PermissionChecker.hasImagePermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateGroupDialog : DialogFragment() {

    private var _binding: DialogGroupCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel : GroupViewModel by viewModels()

    private var imageUri: Uri? = null
    private var clickable = true // 중복 생성을 방지하기 위함

    interface GroupCreationListener {
        fun onGroupCreated()
    }

    private var listener: GroupCreationListener? = null

    fun setGroupCreationListener(listener: GroupCreationListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogGroupCreateBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        onClickListener()
        initObserve()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onClickListener() {
        // 닫기
        binding.createGroupBackTv.setOnClickListener { dismiss() }
        // 확인
        binding.createGroupSaveTv.setOnClickListener {
            if (binding.createGroupTitleEt.toString().isNotEmpty()) {
                if (clickable) { createGroup() }
                clickable = false
            } else {
                Toast.makeText(
                    context, "그룹 이름을 올바르게 등록해주세요.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        // 앨범 권한 확인 후 연결
        binding.createGroupCoverImgIv.setOnClickListener {
            openGallery()
        }
    }

    private fun initObserve() {
        viewModel.addGroupResult.observe(viewLifecycleOwner) {
            if(it.groupId != 0L) {
                Toast.makeText(requireContext(), "그룹 생성에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                listener?.onGroupCreated()
                dismiss()
            }
        }
    }
    private fun createGroup() {
        if (!NetworkCheckerImpl(requireContext()).isOnline()) {
            return
        }
        viewModel.addGroup(imageUri, binding.createGroupTitleEt.text.toString())
    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        if (hasImagePermission(requireContext())) {
            val galleryIntent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                type = "image/*"
                data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            getImage.launch(galleryIntent)
        } else {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
            ActivityCompat.requestPermissions(requireActivity(), permissions, 200)
        }
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let {
                imageUri = result.data!!.data
                if (imageUri != null) {
                    Glide.with(this)
                        .load(imageUri)
                        .fitCenter()
                        .apply(RequestOptions().override(500,500))
                        .into(binding.createGroupCoverImgIv)
                }
            }
        }
    }
}