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

class DiaryGalleryRVAdapter(
    // 다이어리 리스트의 이미지(둥근 모서리, 점선 테두리 X)
    private val context: Context,
    private val imgList: List<String>?,
    private val imageClickListener: (String) -> Unit
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

        val context = context
        val uri = imgList?.get(position)

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)

        Glide.with(context)
            .load(uri)
            .transform(CenterCrop(), RoundedCorners(30)) // centerCrop, 이미지 모서리 설정
            .apply(requestOptions)
            .into(holder.imageUrl)

        holder.imageUrl.setOnClickListener {
            if (uri != null) {
                imageClickListener(uri)
            }
        }

    }

    override fun getItemCount(): Int = imgList?.size ?: 0

    inner class ViewHolder(val binding: ItemDiaryListGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageUrl = binding.galleryImgIv
    }
}

