package com.mongmong.namo.presentation.ui.diary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemGalleryListBinding
import com.mongmong.namo.domain.model.DiaryImage

class GalleryImageRVAdapter(
    private val isMoimMemo: Boolean,
    val deleteClickListener: (diaryImage: DiaryImage) -> Unit,
    val imageClickListener: () -> Unit
) : RecyclerView.Adapter<GalleryImageRVAdapter.ViewHolder>() {

    private val items = ArrayList<DiaryImage>()

    fun addImages(imgs: List<DiaryImage>) {
        this.items.clear()
        this.items.addAll(imgs)
        notifyDataSetChanged()
    }

    private fun removeImage(position: Int) {
        val removedImage = items.removeAt(position)
        notifyItemRemoved(position)
        deleteClickListener(removedImage)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryListBinding = ItemGalleryListBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
        Glide.with(holder.binding.galleryImgIv.context)
            .load(items[position].url)
            .apply(requestOptions)
            .into(holder.binding.galleryImgIv)

        holder.binding.galleryDeleteBtn.visibility = if (isMoimMemo) View.GONE else View.VISIBLE

        holder.binding.galleryDeleteBtn.setOnClickListener {
            removeImage(position)
        }

        holder.binding.galleryImgIv.setOnClickListener {
            imageClickListener()
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemGalleryListBinding) : RecyclerView.ViewHolder(binding.root)
}
