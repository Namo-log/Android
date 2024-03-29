package com.mongmong.namo.presentation.ui.bottom.diary.personalDiary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemGalleryListBinding

class GalleryListAdapter(  // 다이어리 추가, 수정 화면의 이미지(점선 테두리 O)
    private val context: Context,
):
    RecyclerView.Adapter<GalleryListAdapter.ViewHolder>(){

    private val items = ArrayList<String?>()

    @SuppressLint("NotifyDataSetChanged")
    fun addImages(image: List<String?>) {
        this.items.clear()
        this.items.addAll(image)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryListBinding = ItemGalleryListBinding.inflate(
            LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri= items[position]

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)

        Glide.with(context)
            .load(uri)
            .apply(requestOptions)
            .into(holder.imageUrl)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemGalleryListBinding): RecyclerView.ViewHolder(binding.root){
        val imageUrl=binding.galleryImgIv
    }
}