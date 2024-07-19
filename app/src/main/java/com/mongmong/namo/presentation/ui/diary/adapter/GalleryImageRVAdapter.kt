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

class GalleryImageRVAdapter(  // 다이어리 추가, 수정 화면의 이미지(점선 테두리 O)
    private val isMoimMemo: Boolean,
    val deleteClickListener: (newImages: List<DiaryImage>) -> Unit,
    val imageClickListener: () -> Unit
) : RecyclerView.Adapter<GalleryImageRVAdapter.ViewHolder>() {

    private val items = ArrayList<DiaryImage>()

    fun addImages(imgs: List<DiaryImage>) {
        this.items.clear()
        this.items.addAll(imgs)
        notifyDataSetChanged()
    }

    fun removeImage(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
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
            val imageId = items[position].id
            removeImage(position)
            deleteClickListener(items)
        }

        holder.binding.galleryImgIv.setOnClickListener {
            imageClickListener()
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemGalleryListBinding) : RecyclerView.ViewHolder(binding.root)
}
