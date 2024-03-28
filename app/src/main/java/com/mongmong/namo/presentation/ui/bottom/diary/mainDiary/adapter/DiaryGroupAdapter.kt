package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary.adapter


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
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import com.mongmong.namo.data.remote.diary.DiaryRepository
import com.mongmong.namo.databinding.ItemDiaryItemListBinding
import com.mongmong.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class DiaryGroupAdapter(  // 월 별 그룹 다이어리 리스트 어댑터
    val detailClickListener: (DiarySchedule) -> Unit,
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
        when (holder) {
            is DiaryHeaderViewHolder -> {
                val diaryItem = getItem(position) as DiarySchedule
                holder.bind(diaryItem)
            }
            is DiaryContentViewHolder -> {
                val diaryItems = getItem(position) as DiarySchedule
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
        private val context: Context,
        private val imageClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        val onclick = binding.editLy

        @SuppressLint("ResourceAsColor")
        fun bind(item: DiarySchedule) {
            binding.apply {

                itemDiaryContentTv.text = item.content
                itemDiaryTitleTv.text = item.title

                setViewMore(itemDiaryContentTv, viewMore)

                val repo = DiaryRepository(context)

                val category =
                    repo.getCategory(item.categoryId, item.categoryId)

                context.resources?.let {
                    binding.itemDiaryCategoryColorIv.background.setTint(category.color)
                }

                val adapter =
                    DiaryGalleryRVAdapter(context, item.images, imageClickListener)
                diaryGalleryRv.adapter = adapter
                diaryGalleryRv.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                if (itemDiaryContentTv.text.isNullOrEmpty()) itemDiaryContentTv.visibility =
                    View.GONE
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                imageClickListener: (String) -> Unit
            ): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDiaryItemListBinding.inflate(layoutInflater, parent, false)
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