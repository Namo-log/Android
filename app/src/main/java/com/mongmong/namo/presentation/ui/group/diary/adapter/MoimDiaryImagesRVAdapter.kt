package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemGalleryListBinding
import com.mongmong.namo.domain.model.DiaryImage

class MoimDiaryImagesRVAdapter(
    private val itemClickListener: () -> Unit,
    private val deleteImageClickListener: (DiaryImage) -> Unit
) : RecyclerView.Adapter<MoimDiaryImagesRVAdapter.ViewHolder>() {

    private val items: MutableList<DiaryImage> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: List<DiaryImage>) {
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
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemGalleryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiaryImage) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)

            Glide.with(binding.galleryImgIv)
                .load(item.imageUrl)
                .apply(requestOptions)
                .into(binding.galleryImgIv)

            binding.galleryDeleteBtn.setOnClickListener {
                deleteImageClickListener(item)
            }

            binding.galleryImgIv.setOnClickListener {
                itemClickListener()
            }
        }
    }
}
