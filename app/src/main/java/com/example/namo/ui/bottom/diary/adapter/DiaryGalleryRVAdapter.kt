package com.example.namo.ui.bottom.diary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.namo.databinding.ItemDiaryListGalleryBinding
import com.example.namo.ui.bottom.diary.Gallery


class DiaryGalleryRVAdapter(
    private val imgList:MutableList<Gallery>,
    private val context: Context
):
    RecyclerView.Adapter<DiaryGalleryRVAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListGalleryBinding = ItemDiaryListGalleryBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context)
            .load(imgList[position].img)
            .into(holder.image)

        //Preload
        if (position <= imgList.size) {
            val endPosition = if (position + 1 > imgList.size) {
                imgList.size
            } else {
                position + 1
            }
            imgList.subList(position, endPosition )
                .map {
                    it }
                .forEach {
                    preload(context, it.img)
            }
        }
    }

    override fun getItemCount(): Int = imgList.size

    inner class ViewHolder(val binding: ItemDiaryListGalleryBinding): RecyclerView.ViewHolder(binding.root){

        val image=binding.galleryImgIv
    }

    private fun preload(context: Context, img:Int) {
        Glide.with(context).load(img)
            .preload(150, 150)
    }
}

