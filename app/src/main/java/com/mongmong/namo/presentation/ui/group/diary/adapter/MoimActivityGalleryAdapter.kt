package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.databinding.ItemGalleryListBinding
import com.mongmong.namo.generated.callback.OnClickListener
import kotlinx.coroutines.*

class MoimActivityGalleryAdapter(
    private val itemClickListener: () -> Unit,
    private val deleteImageClickListener: (String) -> Unit
) :
    RecyclerView.Adapter<MoimActivityGalleryAdapter.ViewHolder>() {


    private val items: ArrayList<String> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: List<String>) {
        this.items.clear()
        this.items.addAll(image)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(image: String) {
        this.items.remove(image)
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
        holder.itemView.setOnClickListener {
            itemClickListener()
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemGalleryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(item: String) {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)

                Glide.with(binding.galleryImgIv)
                    .load(item)
                    .apply(requestOptions)
                    .into(binding.galleryImgIv)

                binding.galleryDeleteBtn.setOnClickListener {
                    deleteItem(item)
                    deleteImageClickListener(item)
                }
            }
    }
}