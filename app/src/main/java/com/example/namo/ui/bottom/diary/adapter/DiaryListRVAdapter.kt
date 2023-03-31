package com.example.namo.ui.bottom.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat

class DiaryListRVAdapter(
    val context: Context,
    var list: List<Event>,
):
    RecyclerView.Adapter<DiaryListRVAdapter.ViewHolder>() {

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryEditInterface {
        fun onEditClicked(allData: Event)
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

        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Event) {

            binding.itemDiaryContentTv.text
            binding.itemDiaryContentTv.text=item.content
            binding.itemDiaryTitleTv.text=item.title
            binding.itemDiaryCategoryColorIv.background.setTint(ContextCompat.getColor(context,item.categoryColor))
            binding.diaryGalleryRv.adapter= DiaryGalleryRVAdapter(context, item.imgs)
            binding.diaryGalleryRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            if(item.content.isEmpty()) binding.itemDiaryContentTv.visibility= View.GONE
            if(item.imgs?.isEmpty() == true) binding.diaryGalleryRv.visibility=View.GONE

        }
    }
}

