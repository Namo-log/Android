package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimDiaryActivityBinding
import com.mongmong.namo.databinding.ItemMoimDiaryDiaryBinding
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.group.MoimActivity

class MoimDiaryVPAdapter(
    private val diaryEventListener: OnDiaryEventListener,
    private val activityEventListener: OnActivityEventListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val activities = mutableListOf<MoimActivity>()
    private var diary = DiaryDetail("", 0L, emptyList(), 3)

    fun updateDiary(diary: DiaryDetail) {
        this.diary = diary
        notifyDataSetChanged()
    }

    fun submitActivities(newActivities: List<MoimActivity>) {
        activities.clear()
        activities.addAll(newActivities)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = 1 + activities.size // diary 하나 + activities

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_DIARY else VIEW_TYPE_ACTIVITY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DIARY -> {
                DiaryViewHolder(
                    ItemMoimDiaryDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            VIEW_TYPE_ACTIVITY -> {
                ActivityViewHolder(
                    ItemMoimDiaryActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    activityEventListener)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ActivityViewHolder) {
            val activity = activities[position - 1] // 첫 번째는 diary이므로 인덱스 조정
            holder.bind(activity)
        } else if (holder is DiaryViewHolder) {
            holder.bind(diary)
        }
    }

    inner class DiaryViewHolder(private val binding: ItemMoimDiaryDiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(diary: DiaryDetail) {
            binding.diary = diary

            // Content 변경 이벤트 처리
            binding.diaryContentEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    diaryEventListener.onContentChanged(s.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Enjoy 클릭 이벤트 처리
            binding.diaryEnjoy1Iv.setOnClickListener { diaryEventListener.onEnjoyClicked(1) }
            binding.diaryEnjoy2Iv.setOnClickListener { diaryEventListener.onEnjoyClicked(2) }
            binding.diaryEnjoy3Iv.setOnClickListener { diaryEventListener.onEnjoyClicked(3) }

            // 이미지 리스트 어댑터 설정
            val adapter = MoimDiaryImagesRVAdapter(
                itemClickListener = { /* 이미지 클릭 시 동작 */ },
                deleteImageClickListener = { diaryImage ->
                    diaryEventListener.onDeleteImage(diaryImage)
                }
            )
            binding.moimGalleryRv.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter.addItem(diary.diaryImages)
        }
    }

    inner class ActivityViewHolder(
        private val binding: ItemMoimDiaryActivityBinding,
        private val listener: OnActivityEventListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: MoimActivity) {

            // 여기서 콜백을 설정합니다.
            binding.activityDeleteBtn.setOnClickListener {
                listener.onDeleteActivity()
            }
            binding.updateImageIv.setOnClickListener {
                listener.onUpdateImage(bindingAdapterPosition)
            }
            binding.activityTitleTv.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    listener.onActivityNameChanged(s.toString(), bindingAdapterPosition - 1)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            // 기타 이벤트 리스너 설정
        }
    }

    companion object {
        private const val VIEW_TYPE_DIARY = 0
        private const val VIEW_TYPE_ACTIVITY = 1
    }

    interface OnActivityEventListener {
        fun onDeleteActivity()
        fun onUpdateImage(position: Int)
        fun onActivityNameChanged(name: String, position: Int)
        // 기타 필요한 콜백 메서드들
    }

    interface OnDiaryEventListener {
        fun onImageClicked(images: List<DiaryImage>)
        fun onContentChanged(content: String)
        fun onEnjoyClicked(enjoyRating: Int)
        fun onDeleteImage(image: DiaryImage)
        // 기타 필요한 메서드 추가
    }
}


