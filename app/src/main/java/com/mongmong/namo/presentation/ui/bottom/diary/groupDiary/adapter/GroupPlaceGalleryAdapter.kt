package com.mongmong.namo.presentation.ui.bottom.diary.groupDiary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemGalleryListBinding
import kotlinx.coroutines.*

class GroupPlaceGalleryAdapter(
    // 그룹 다이어리 장소별 이미지
    private val context: Context,
) :
    RecyclerView.Adapter<GroupPlaceGalleryAdapter.ViewHolder>() {

    private val items: ArrayList<String?> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: List<String?>) {
        this.items.clear()
        this.items.addAll(image)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryListBinding = ItemGalleryListBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri = items[position]

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)

        Glide.with(context)
            .load(uri)
            .apply(requestOptions)
            .into(holder.imageUrl)

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemGalleryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageUrl = binding.galleryImgIv

    }
}