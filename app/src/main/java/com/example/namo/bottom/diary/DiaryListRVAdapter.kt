package com.example.namo.bottom.diary

import DiaryGalleryRVAdapter
import YearMonthDialog.Companion.month
import YearMonthDialog.Companion.year
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
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryListRVAdapter(
    val context: Context,
    var list: MutableList<DiaryDummy>
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

    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: DiaryDummy) {

            val num= DecimalFormat("00")
            var months=num.format(item.month)
            var dates=num.format(item.date)

            binding.diary = item
            binding.itemDiaryCategoryColorIv.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.category))
            binding.diaryGalleryRv.adapter = DiaryGalleryRVAdapter(item.rv)
            binding.diaryGalleryRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.diaryDayTv.text= "${item.year}-${months}-${dates}"

        }
    }
}
