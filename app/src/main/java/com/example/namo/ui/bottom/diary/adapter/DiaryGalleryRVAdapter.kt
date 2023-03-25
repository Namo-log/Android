package com.example.namo.ui.bottom.diary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.namo.databinding.ItemDiaryListGalleryBinding


class DiaryGalleryRVAdapter(
    private val context: Context,
    private val imgList: List<String>?,
):
    RecyclerView.Adapter<DiaryGalleryRVAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListGalleryBinding = ItemDiaryListGalleryBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri= imgList?.get(position)
        Glide.with(context)
            .load(uri?.toUri())
            .into(holder.imageUrl)
    }

    override fun getItemCount(): Int = imgList!!.size

    inner class ViewHolder(val binding: ItemDiaryListGalleryBinding): RecyclerView.ViewHolder(binding.root){
       val imageUrl=binding.galleryImgIv
    }
}

