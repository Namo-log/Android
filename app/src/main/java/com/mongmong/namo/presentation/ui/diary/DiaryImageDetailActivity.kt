package com.mongmong.namo.presentation.ui.diary

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityDiaryImageDetailBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.diary.adapter.ImageDetailVPAdapter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DiaryImageDetailActivity : BaseActivity<ActivityDiaryImageDetailBinding>(R.layout.activity_diary_image_detail) {
    private lateinit var imagePagerAdapter: ImageDetailVPAdapter
    private lateinit var imgs: List<String>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadImage()
        } else {
            Snackbar.make(binding.root, "저장 권한이 필요합니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun setup() {
        imgs = intent.getStringArrayListExtra("imgs") as List<String>
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
        binding.backBtn.setOnClickListener { finish() }
        binding.downloadBtnIv.setOnClickListener { checkPermissionAndDownload() }
    }

    private fun checkPermissionAndDownload() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> { downloadImage() }
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                downloadImage()
            }
            else -> { requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
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
            Snackbar.make(binding.root, "이미지가 저장되었습니다.", Snackbar.LENGTH_SHORT).show()
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
}
