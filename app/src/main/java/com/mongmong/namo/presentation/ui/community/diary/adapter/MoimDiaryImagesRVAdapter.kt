package com.mongmong.namo.presentation.ui.community.diary.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemMoimDiaryImageBinding
import com.mongmong.namo.domain.model.DiaryImage

class MoimDiaryImagesRVAdapter(
    private val itemClickListener: () -> Unit,
    private val deleteClickListener: (DiaryImage) -> Unit,
    private val isEditMode: Boolean
) : RecyclerView.Adapter<MoimDiaryImagesRVAdapter.ViewHolder>() {

    private val items: MutableList<DiaryImage> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: List<DiaryImage>) {
        this.items.clear()
        this.items.addAll(image)
        notifyDataSetChanged()
    }

    private fun removeImage(position: Int) {
        val removedImage = items.removeAt(position)
        notifyItemRemoved(position)
        deleteClickListener(removedImage)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMoimDiaryImageBinding = ItemMoimDiaryImageBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemMoimDiaryImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiaryImage) {
            binding.isEdit = isEditMode

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
            Log.d("MoimDiaryImagesRVAdapter", "${item.imageUrl}")

            Glide.with(binding.imageIv.context)
                .load(item.imageUrl)
                .apply(requestOptions)
                .into(binding.imageIv)

            binding.imageDeleteBtn.setOnClickListener {
                removeImage(bindingAdapterPosition)
            }

            binding.imageIv.setOnClickListener {
                itemClickListener()
            }
        }
    }
}
