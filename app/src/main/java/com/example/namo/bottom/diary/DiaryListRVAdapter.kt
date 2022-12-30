package com.example.namo.bottom.diary

import DiaryGalleryRVAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemDiaryListBinding

class DiaryListRVAdapter(val context: Context, var list: MutableList<DiaryDummy>):
    RecyclerView.Adapter<DiaryListRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListBinding =
            ItemDiaryListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiaryDummy) {

            binding.diary = item
            binding.itemDiaryCategoryColorIv.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.category))
            binding.diaryGalleryRv.adapter = DiaryGalleryRVAdapter(item.rv)
            binding.diaryGalleryRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }
    }
}

