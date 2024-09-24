package com.mongmong.namo.presentation.ui.diary.adapter

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

class DiaryImagesRVAdapter(
    private val imgList: List<DiaryImage>,
    private val imageClickListener: (List<DiaryImage>) -> Unit
) : RecyclerView.Adapter<DiaryImagesRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListGalleryBinding = ItemDiaryListGalleryBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position])

        holder.binding.galleryImgIv.setOnClickListener {
            if(imgList != null) imageClickListener(imgList)
        }

    }

    override fun getItemCount(): Int = imgList.size

    inner class ViewHolder(val binding: ItemDiaryListGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiaryImage) {
            Glide.with(binding.galleryImgIv)
                .load(item.imageUrl)
                .transform(CenterCrop(), RoundedCorners(30)) // centerCrop, 이미지 모서리 설정
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(binding.galleryImgIv)
        }

    }
}

