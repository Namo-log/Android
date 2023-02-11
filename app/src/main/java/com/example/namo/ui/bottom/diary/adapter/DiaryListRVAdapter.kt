package com.example.namo.ui.bottom.diary.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemDiaryListBinding
import com.example.namo.ui.bottom.diary.Diary

class DiaryListRVAdapter(
    val context: Context,
    var list: MutableList<Diary>,
    private val onItemClicked:(position:Int)->Unit
):
    RecyclerView.Adapter<DiaryListRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryListBinding =
            ItemDiaryListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(list[position])

        holder.apply {
            binding.diaryEditTv.setOnClickListener {
                onItemClicked(position)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Diary) {

            val dateTime=item.date.toString("yyyy-MM-dd")

            binding.diary = item
            binding.itemDiaryCategoryColorIv.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.category))
            binding.diaryGalleryRv.adapter = DiaryGalleryRVAdapter(item.rv,context)
            binding.diaryGalleryRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.diaryDayTv.text=dateTime
        }
    }
}

