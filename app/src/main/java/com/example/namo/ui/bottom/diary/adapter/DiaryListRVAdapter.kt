package com.example.namo.ui.bottom.diary.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.Diary
import com.example.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat


class DiaryListRVAdapter(
    val context: Context,
    var list: List<Diary>,
):
    RecyclerView.Adapter<DiaryListRVAdapter.ViewHolder>() {

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryEditInterface {
        fun onEditClicked(diary: Diary)
    }
    private lateinit var diaryRecordClickListener: DiaryEditInterface
    fun setRecordClickListener(itemClickListener: DiaryEditInterface){
        diaryRecordClickListener=itemClickListener
    }
    /** ----- **/

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
                diaryRecordClickListener.onEditClicked(list[position])
            }
        }
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Diary) {

            val formattedDate= SimpleDateFormat("yyyy.MM.dd").format(item.date)

            binding.diary = item
            binding.diaryDayTv.text=formattedDate
            binding.itemDiaryCategoryColorIv.background.setTint(context.resources.getColor(item.categoryColor))
            binding.diaryGalleryRv.adapter=DiaryGalleryRVAdapter(context,item.imgList)
            binding.diaryGalleryRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }
    }
}

