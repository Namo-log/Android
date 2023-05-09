package com.example.namo.ui.bottom.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat

class DiaryMultiAdapter(
    val context: Context,
    private val items: ArrayList<TaskListItem> = arrayListOf<TaskListItem>()
) : RecyclerView.Adapter<DiaryMultiAdapter.TaskViewHolder>() {


    /** 기록 아이템 클릭 리스너 **/
    interface DiaryEditInterface {
        fun onEditClicked(allData: TaskListItem)
    }

    private lateinit var diaryRecordClickListener: DiaryEditInterface
    fun setRecordClickListener(itemClickListener: DiaryEditInterface) {
        diaryRecordClickListener = itemClickListener
    }

    /** ----- **/

    private fun getItem(position: Int): TaskListItem = this.items[position]

    override fun getItemCount(): Int = this.items.size

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            TaskListItem.Header.VIEW_TYPE -> TaskHeaderViewHolder(itemView)
            TaskListItem.Item.VIEW_TYPE -> TaskItemViewHolder(itemView)
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))

//        holder.itemView..setOnClickListener {
//            diaryRecordClickListener?.let {
//                it(getItem(position))
//            }
//        holder.apply {
//                    bind.diaryEditTv.setOnClickListener {
//                        diaryRecordClickListener.onEditClicked(items[position])
//                    }
//                }
//            }
        }

    abstract class TaskViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: TaskListItem)
    }

    class TaskHeaderViewHolder(
        itemView: View
    ) : TaskViewHolder(itemView) {

        private val binding by lazy { ItemDiaryListBinding.bind(itemView) }

        @SuppressLint("SimpleDateFormat")
        override fun bind(item: TaskListItem) {
            val task = (item as TaskListItem.Header).task
            binding.apply {
                val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(task.startLong)
                binding.diaryDayTv.text = formattedDate
            }
        }
    }

    class TaskItemViewHolder(
        itemView: View
    ) : TaskViewHolder(itemView) {

        private val binding by lazy { ItemDiaryDateListBinding.bind(itemView) }

        override fun bind(item: TaskListItem) {
            val task = (item as TaskListItem.Item).task

            binding.apply {
                binding.itemDiaryContentTv.text
                binding.itemDiaryContentTv.text = task.content
                binding.itemDiaryTitleTv.text = task.title
                binding.itemDiaryCategoryColorIv.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        task.categoryColor
                    )
                )
                binding.diaryGalleryRv.adapter = DiaryGalleryRVAdapter(itemView.context, task.imgs)
                binding.diaryGalleryRv.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

                if (task.content.isEmpty()) binding.itemDiaryContentTv.visibility = View.GONE
                if (task.imgs?.isEmpty() == true) binding.diaryGalleryRv.visibility = View.GONE

            }
        }
    }
}