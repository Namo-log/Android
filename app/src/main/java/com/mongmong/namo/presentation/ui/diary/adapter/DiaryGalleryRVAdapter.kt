package com.mongmong.namo.presentation.ui.diary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemDiaryListGalleryBinding
import com.mongmong.namo.domain.model.DiaryImage

class DiaryGalleryRVAdapter(
    // 다이어리 리스트의 이미지(둥근 모서리, 점선 테두리 X)
    private val imgList: List<DiaryImage>?,
    private val imageClickListener: (List<DiaryImage>) -> Unit
) :
    RecyclerView.Adapter<DiaryGalleryRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListGalleryBinding = ItemDiaryListGalleryBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList?.get(position) ?: DiaryImage(0L, ""))

        holder.binding.galleryImgIv.setOnClickListener {
            if(imgList != null) imageClickListener(imgList)
        }

    }

    override fun getItemCount(): Int = imgList?.size ?: 0

    inner class ViewHolder(val binding: ItemDiaryListGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiaryImage) {
            Glide.with(binding.galleryImgIv)
                .load(item.url)
                .transform(CenterCrop(), RoundedCorners(30)) // centerCrop, 이미지 모서리 설정
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(binding.galleryImgIv)
        }

    }
}

