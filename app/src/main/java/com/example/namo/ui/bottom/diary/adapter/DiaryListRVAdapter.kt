package com.example.namo.ui.bottom.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.DiaryList
import com.example.namo.databinding.ItemDiaryListBinding
import java.lang.Boolean.TRUE
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

        val formattedDate= SimpleDateFormat("yyyy.MM.dd").format(list[position].event_start)
        holder.apply {
            binding.diaryEditTv.setOnClickListener {
                diaryRecordClickListener.onEditClicked(list[position])
            }

            bind(list[position])
            binding.diaryDayTv.text=formattedDate
            binding.diaryDateLayout.visibility=View.VISIBLE


        }


    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: DiaryList) {

            binding.itemDiaryContentTv.text=item.diary_content
            binding.itemDiaryTitleTv.text=item.event_title
            binding.itemDiaryCategoryColorIv.background.setTint(ContextCompat.getColor(context,item.event_category_color))
            binding.diaryGalleryRv.adapter=DiaryGalleryRVAdapter(context,item.diary_img)
            binding.diaryGalleryRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            if(item.diary_content.isEmpty()) binding.itemDiaryContentTv.visibility= View.GONE
            if(item.diary_img.isNullOrEmpty()) binding.diaryGalleryRv.visibility=View.GONE

        }
        val a=binding.diaryDayTv.text
    }
}

