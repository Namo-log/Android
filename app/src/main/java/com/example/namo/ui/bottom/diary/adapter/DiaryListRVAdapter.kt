package com.example.namo.ui.bottom.diary.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.DiaryList
import com.example.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat

class DiaryListRVAdapter(
    val context: Context,
    var list: List<DiaryList>,
):
    RecyclerView.Adapter<DiaryListRVAdapter.ViewHolder>() {

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryEditInterface {
        fun onEditClicked(allData: DiaryList)
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
        fun bind(item: DiaryList) {

            val formattedDate= SimpleDateFormat("yyyy.MM.dd").format(item.event_start)

            binding.itemDiaryContentTv.text
            binding.diaryDayTv.text=formattedDate
            binding.itemDiaryContentTv.text=item.diary_content
            binding.itemDiaryTitleTv.text=item.event_title
            binding.itemDiaryCategoryColorIv.background.setTint(context.resources.getColor(item.event_category_color))
            binding.diaryGalleryRv.adapter=DiaryGalleryRVAdapter(context,item.diary_img)
            binding.diaryGalleryRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }
    }
}

