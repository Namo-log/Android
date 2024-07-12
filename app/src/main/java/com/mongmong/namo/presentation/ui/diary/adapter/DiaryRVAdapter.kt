package com.mongmong.namo.presentation.ui.diary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.databinding.ItemDiaryItemListBinding
import com.mongmong.namo.databinding.ItemDiaryListBinding
import com.mongmong.namo.presentation.config.CategoryColor
import java.text.SimpleDateFormat

class DiaryRVAdapter(
    val personalEditClickListener: ((DiarySchedule) -> Unit)? = null,
    val moimEditClickListener: ((Long, Int) -> Unit)? = null,
    private val imageClickListener: (String) -> Unit
) : PagingDataAdapter<DiarySchedule, RecyclerView.ViewHolder>(DiaryDiffCallback()) {

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiarySchedule>() {
        override fun areItemsTheSame(oldItem: DiarySchedule, newItem: DiarySchedule): Boolean {
            return oldItem.scheduleId == newItem.scheduleId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiarySchedule, newItem: DiarySchedule): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) as DiarySchedule
        when (holder) {
            is DiaryHeaderViewHolder -> holder.bind(item)
            is DiaryContentViewHolder -> holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> DiaryHeaderViewHolder(
                ItemDiaryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            ITEM_VIEW_TYPE_ITEM -> DiaryContentViewHolder(
                ItemDiaryItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                imageClickListener,
                personalEditClickListener,
                moimEditClickListener
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)!!.isHeader) {
            true -> ITEM_VIEW_TYPE_HEADER
            else -> ITEM_VIEW_TYPE_ITEM
        }
    }

    inner class DiaryHeaderViewHolder(val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiarySchedule) {
            binding.date = SimpleDateFormat("yyyy.MM.dd").format(item.startDate)
        }
    }

    inner class DiaryContentViewHolder(
        private val binding: ItemDiaryItemListBinding,
        private val imageClickListener: (String) -> Unit,
        private val personalEditClickListener: ((DiarySchedule) -> Unit)?,
        private val moimEditClickListener: ((Long, Int) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiarySchedule) {
            setViewMore(binding.itemDiaryContentTv, binding.viewMore)
            binding.diary = item

            binding.itemDiaryCategoryColorIv.backgroundTintList =
                CategoryColor.convertPaletteIdToColorStateList(item.color)

            binding.diaryGalleryRv.apply {
                adapter = DiaryGalleryRVAdapter(context, item.images, imageClickListener)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            binding.editLy.setOnClickListener {
                personalEditClickListener?.invoke(item)
                moimEditClickListener?.invoke(item.scheduleId, item.color)
            }
        }

        private fun setViewMore(contentTextView: TextView, viewMoreTextView: TextView) {
            contentTextView.post {
                val lineCount = contentTextView.layout?.lineCount ?: 0
                if (lineCount > 0) {
                    if ((contentTextView.layout?.getEllipsisCount(lineCount - 1) ?: 0) > 0) {
                        viewMoreTextView.visibility = View.VISIBLE

                        viewMoreTextView.setOnClickListener {
                            contentTextView.maxLines = Int.MAX_VALUE
                            viewMoreTextView.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_ITEM = 1
    }
}
