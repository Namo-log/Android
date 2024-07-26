package com.mongmong.namo.presentation.ui.diary.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mongmong.namo.databinding.ItemDiaryImageDetailBinding

class ImageDetailVPAdapter(var images: List<String>)
    : RecyclerView.Adapter<ImageDetailVPAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemDiaryImageDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: String) {
            Glide.with(binding.imageIv)
                .load(image)
                .into(binding.imageIv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemDiaryImageDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

}
