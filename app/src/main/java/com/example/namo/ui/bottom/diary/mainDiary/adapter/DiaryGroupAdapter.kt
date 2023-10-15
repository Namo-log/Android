package com.example.namo.ui.bottom.diary.mainDiary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.databinding.ItemDiaryGroupListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class DiaryGroupAdapter(  // 월 별 그룹 다이어리 리스트 어댑터
    val detailClickListener: (DiaryGroupItem.Content) -> Unit,
    val imageClickListener: (String) -> Unit
) : ListAdapter<DiaryGroupItem, RecyclerView.ViewHolder>(DiaryDiffCallback()) {


    fun updateData(newData: List<DiaryGroupItem>) {
        submitList(newData)
    }

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryGroupItem>() {
        override fun areItemsTheSame(oldItem: DiaryGroupItem, newItem: DiaryGroupItem): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiaryGroupItem, newItem: DiaryGroupItem): Boolean {
            return oldItem == newItem
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DiaryHeaderViewHolder -> {
                val diaryItem = getItem(position) as DiaryGroupItem.Header
                holder.bind(diaryItem)
            }
            is DiaryContentViewHolder -> {
                val diaryItems = getItem(position) as DiaryGroupItem.Content
                holder.bind(diaryItems)
                holder.onclick.setOnClickListener {
                    detailClickListener(diaryItems)
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
        return when (getItem(position)) {
            is DiaryGroupItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DiaryGroupItem.Content -> ITEM_VIEW_TYPE_ITEM
            else -> throw ClassCastException("Unknown viewType $position")
        }
    }

    class DiaryHeaderViewHolder
    private constructor(private val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(item: DiaryGroupItem.Header) {
            binding.apply {
                val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(item.date)
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
        private val binding: ItemDiaryGroupListBinding,
        private val context: Context,
        private val imageClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        val onclick = binding.editLy

        @SuppressLint("ResourceAsColor")
        fun bind(item: DiaryGroupItem.Content) {
            binding.apply {

                itemDiaryContentTv.text = item.content
                itemDiaryTitleTv.text = item.event_title

                setViewMore(itemDiaryContentTv, viewMore)

                binding.itemDiaryCategoryColorIv.background.setTint(ContextCompat.getColor(context,R.color.MainOrange))

                val adapter =
                    DiaryGalleryRVAdapter(context, item.images, imageClickListener)
                diaryGalleryRv.adapter = adapter
                diaryGalleryRv.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                if (itemDiaryContentTv.text.isNullOrEmpty()) itemDiaryContentTv.visibility = View.GONE
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                imageClickListener: (String) -> Unit
            ): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDiaryGroupListBinding.inflate(layoutInflater, parent, false)
                return DiaryContentViewHolder(binding, parent.context, imageClickListener)
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


}

sealed class DiaryGroupItem {
    abstract val id: Long

    data class Header(override val id: Long, val date: Long) : DiaryGroupItem()

    data class Content(

        var eventId: Long = 0L,
        var event_title: String = "",
        var event_start: Long = 0L,
        var event_category_idx: Long = 0L,
        var event_place_name: String = "없음",
        var content: String?,
        var images: List<String>? = null,

        override val id: Long

    ) : DiaryGroupItem(),java.io.Serializable


}