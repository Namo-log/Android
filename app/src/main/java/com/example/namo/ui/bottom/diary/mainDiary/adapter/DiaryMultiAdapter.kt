package com.example.namo.ui.bottom.diary.mainDiary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import com.example.namo.ui.bottom.diary.adapter.TaskListItem
import java.text.SimpleDateFormat

class DiaryMultiAdapter( // 다이어리 리스트에 그룹 별로 날짜 헤더 추가
    val context: Context,
    private val items: ArrayList<TaskListItem> = arrayListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryEditInterface {
        fun onEditClicked(allData: TaskListItem)
    }
    private lateinit var diaryRecordClickListener: DiaryEditInterface
    fun setRecordClickListener(itemClickListener: DiaryEditInterface) {
        diaryRecordClickListener = itemClickListener
    }

    private fun getItem(position: Int): TaskListItem = this.items[position]

    override fun getItemCount(): Int = this.items.size

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {

        val adapterLayout: View?
        return when(viewType){
            TaskListItem.Header.VIEW_TYPE -> {
                adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_diary_list, parent, false)
                TaskHeaderViewHolder(adapterLayout)
            }
            TaskListItem.Item.VIEW_TYPE -> {
                adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_diary_date_list, parent, false)
                TaskItemViewHolder(adapterLayout)
            }
            else -> throw RuntimeException("알 수 없는 뷰 타입")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item=getItem(position)

        when(item.layoutId){
            TaskListItem.Item.VIEW_TYPE ->{
                (holder as TaskItemViewHolder).bind(item)
                holder.onclick.setOnClickListener {
                    diaryRecordClickListener.onEditClicked(item)
                }
            }
            TaskListItem.Header.VIEW_TYPE ->{
                (holder as TaskHeaderViewHolder).bind(item)
            }
        }
    }

    class TaskHeaderViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by lazy { ItemDiaryListBinding.bind(itemView) }
        @SuppressLint("SimpleDateFormat")
        fun bind(item: TaskListItem) {
            val task = (item as TaskListItem.Header).task
            binding.apply {
                val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(task.startLong)
                diaryDayTv.text = formattedDate
            }
        }
    }

    class TaskItemViewHolder(
        itemView: View
    ) :  RecyclerView.ViewHolder(itemView) {

        private val binding by lazy { ItemDiaryDateListBinding.bind(itemView) }

        val onclick=binding.diaryEditTv
         fun bind(item: TaskListItem) {
            val task = (item as TaskListItem.Item).task

            binding.apply {
                itemDiaryContentTv.text
                itemDiaryContentTv.text = task.content
                itemDiaryTitleTv.text = task.title
                itemDiaryCategoryColorIv.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        task.categoryColor
                    )
                )
                diaryGalleryRv.adapter = DiaryGalleryRVAdapter(itemView.context, task.imgs)
                diaryGalleryRv.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

                if (task.content.isEmpty()) itemDiaryContentTv.visibility = View.GONE
                if (task.imgs?.isEmpty() == true) diaryGalleryRv.visibility = View.GONE
            }
        }
    }
}