package com.mongmong.namo.presentation.ui.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
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



class DiaryAdapter( // 월 별 개인 다이어리 리스트 어댑터
    val editClickListener: (DiarySchedule) -> Unit,
    val imageClickListener: (String) -> Unit
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
            is DiaryContentViewHolder -> {
                holder.bind(item)
                holder.onclick.setOnClickListener {
                    editClickListener(item)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> DiaryHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> DiaryContentViewHolder.from(parent, imageClickListener)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)!!.isHeader) {
            true -> ITEM_VIEW_TYPE_HEADER
            else -> ITEM_VIEW_TYPE_ITEM
        }
    }


    class DiaryHeaderViewHolder
    private constructor(private val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(item: DiarySchedule) {
            binding.apply {
                val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(item.startDate)
                diaryDayTv.text = formattedDate
            }
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDiaryListBinding.inflate(layoutInflater, parent, false)
                return DiaryHeaderViewHolder(binding)
            }
        }
    }

    class DiaryContentViewHolder private constructor(
        private val binding: ItemDiaryItemListBinding,
        private val imageClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        val onclick = binding.editLy

        fun bind(item: DiarySchedule) {
            setViewMore(binding.itemDiaryContentTv, binding.viewMore)
            binding.diary = item

            binding.diaryGalleryRv.apply {
                adapter = DiaryGalleryRVAdapter(context, item.images, imageClickListener)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                imageClickListener: (String) -> Unit
            ): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDiaryItemListBinding.inflate(layoutInflater, parent, false)
                return DiaryContentViewHolder(binding, imageClickListener)
            }
        }

        private fun setViewMore(contentTextView: TextView, viewMoreTextView: TextView) {
            // getEllipsisCount()을 통한 더보기 표시 및 구현
            contentTextView.post {
                val lineCount = contentTextView.layout?.lineCount ?: 0
                if (lineCount > 0) {
                    if ((contentTextView.layout?.getEllipsisCount(lineCount - 1) ?: 0) > 0) {
                        // 더보기 표시
                        viewMoreTextView.visibility = View.VISIBLE

                        // 더보기 클릭 이벤트
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
