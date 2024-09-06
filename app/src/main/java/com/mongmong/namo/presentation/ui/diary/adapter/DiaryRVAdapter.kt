package com.mongmong.namo.presentation.ui.diary.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryCollectionBinding
import com.mongmong.namo.databinding.ItemDiaryListBinding
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.presentation.utils.DiaryDateConverter.toDiaryHeaderDate

class DiaryRVAdapter(
    val personalEditClickListener: ((Diary) -> Unit)? = null,
    val moimEditClickListener: ((Long) -> Unit)? = null,
    val participantClickListener: (Int, String) -> Unit,
    private val imageClickListener: (List<DiaryImage>) -> Unit
) : PagingDataAdapter<Diary, RecyclerView.ViewHolder>(DiaryDiffCallback()) {

    class DiaryDiffCallback : DiffUtil.ItemCallback<Diary>() {
        override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem.scheduleId == newItem.scheduleId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) as Diary
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
                ItemDiaryCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
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
        fun bind(item: Diary) {
            binding.date = item.startDate.toDiaryHeaderDate()
        }
    }

    inner class DiaryContentViewHolder(
        private val binding: ItemDiaryCollectionBinding,
        private val imageClickListener: (List<DiaryImage>) -> Unit,
        private val personalEditClickListener: ((Diary) -> Unit)?,
        private val moimEditClickListener: ((Long) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Diary) {
            binding.diary = item

            /*binding.itemDiaryCollectionCategoryIv.backgroundTintList =
                CategoryColor.convertPaletteIdToColorStateList(item.color)*/

            binding.diaryGalleryRv.apply {
                if(!item.diarySummary.diaryImages.isNullOrEmpty()){
                    Log.d("DiaryRVAdpater", "images: ${item.diarySummary.diaryImages}")
                    Log.d("DiaryRVAdpater", "date: ${item.startDate}")
                    Log.d("DiaryRVAdpater", "category: ${item.categoryInfo.color}")
                    adapter = DiaryGalleryRVAdapter(item.diarySummary.diaryImages, imageClickListener)
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                }
            }

            binding.itemDiaryCollectionParticipantTv.setOnClickListener {
                participantClickListener(item.participantInfo.count, item.participantInfo.names)
            }

            binding.root.setOnClickListener {
                personalEditClickListener?.invoke(item)
                moimEditClickListener?.invoke(item.scheduleId)
            }
        }
    }

    companion object {
        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_ITEM = 1
    }
}
