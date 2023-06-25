package com.example.namo.ui.bottom.diary.mainDiary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.namo.databinding.ItemDiaryListGalleryBinding

class DiaryGalleryRVAdapter(
    // 다이어리 리스트의 이미지(둥근 모서리, 점선 테두리 X)
    private val context: Context,
    private val imgList: List<String>?,
) :
    RecyclerView.Adapter<DiaryGalleryRVAdapter.ViewHolder>() {

    interface DiaryImageInterface {
        fun onImageClicked(image: String)
    }

    private lateinit var diaryImageClickListener: DiaryImageInterface
    fun setImageClickListener(itemClickListener: DiaryImageInterface) {
        diaryImageClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListGalleryBinding = ItemDiaryListGalleryBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri = imgList?.get(position)
        Glide.with(context)
            .load(uri)
            .into(holder.imageUrl)

        holder.imageUrl.setOnClickListener {
            if (uri != null) {
                diaryImageClickListener.onImageClicked(uri)
            }
        }
    }

    override fun getItemCount(): Int = imgList!!.size

    inner class ViewHolder(val binding: ItemDiaryListGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageUrl = binding.galleryImgIv

    }
}

