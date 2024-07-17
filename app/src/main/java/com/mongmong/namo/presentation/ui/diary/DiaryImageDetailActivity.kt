package com.mongmong.namo.presentation.ui.diary

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.databinding.ActivityDiaryImageDetailBinding
import com.mongmong.namo.presentation.ui.diary.adapter.ImageDetailVPAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DiaryImageDetailActivity : AppCompatActivity(), ConfirmDialogInterface {
    private lateinit var binding: ActivityDiaryImageDetailBinding
    private lateinit var imagePagerAdapter: ImageDetailVPAdapter
    private lateinit var imgs: MutableList<String>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadImage()
        } else {
            Snackbar.make(binding.root, "저장 권한이 필요합니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imgs = intent.getStringArrayListExtra("imgs") as MutableList<String>
        setViewPager()
        initClickListener()
        updatePosition()
    }

    private fun setViewPager() {
        imagePagerAdapter = ImageDetailVPAdapter(imgs)

        binding.diaryImageVp.apply {
            adapter = imagePagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.currentPosition.text = (position + 1).toString()
                }
            })
        }
    }

    private fun initClickListener() {
        // Use OnBackPressedDispatcher to handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithResult()
            }
        })

        binding.backBtn.setOnClickListener { finishWithResult() }
        binding.downloadBtnIv.setOnClickListener { checkPermissionAndDownload() }
        binding.deleteBtnIv.setOnClickListener { showDeleteDialog() }
    }

    private fun checkPermissionAndDownload() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                downloadImage()
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                downloadImage()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun downloadImage() {
        val currentItem = binding.diaryImageVp.currentItem
        if (currentItem >= 0 && currentItem < imgs.size) {
            val imageUrl = imgs[currentItem]
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        saveImageToGallery(resource)
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                })
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "이미지가 저장되었습니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDeleteDialog() {
        val dialog = ConfirmDialog(this, "사진을 정말 삭제하시겠어요?", null, "삭제", 0)
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    private fun deleteImage(image: String) {
        val position = imgs.indexOf(image)
        if (position != -1) {
            imgs.removeAt(position)
            imagePagerAdapter.notifyItemRemoved(position)
            updatePosition()
        }
    }

    private fun updatePosition() {
        with(binding) {
            imagesSize.text = imgs.size.toString()
            when {
                diaryImageVp.currentItem < imgs.size -> currentPosition.text = (diaryImageVp.currentItem + 1).toString()
                imgs.isNotEmpty() -> {
                    diaryImageVp.setCurrentItem(imgs.size - 1, false)
                    currentPosition.text = imgs.size.toString()
                }
                else -> currentPosition.text = "0"
            }
        }
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK, Intent().putStringArrayListExtra("imgs", ArrayList(imgs)))
        finish()
    }

    override fun onClickYesButton(id: Int) {
        val currentItem = binding.diaryImageVp.currentItem
        if (currentItem >= 0 && currentItem < imgs.size) {
            val imageToDelete = imgs[currentItem]
            deleteImage(imageToDelete)
        }
    }
}
