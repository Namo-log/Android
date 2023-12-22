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
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.databinding.ItemDiaryItemListBinding
import com.example.namo.databinding.ItemDiaryListBinding

import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGalleryRVAdapter
import java.text.SimpleDateFormat

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class DiaryAdapter( // 월 별 개인 다이어리 리스트 어댑터
    val editClickListener: (DiaryEvent) -> Unit,
    val imageClickListener: (String) -> Unit
) : PagingDataAdapter<DiaryEvent, RecyclerView.ViewHolder>(DiaryDiffCallback()) {


    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEvent>() {
        override fun areItemsTheSame(oldItem: DiaryEvent, newItem: DiaryEvent): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiaryEvent, newItem: DiaryEvent): Boolean {
            return oldItem == newItem
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DiaryHeaderViewHolder -> {
                val diaryItem = getItem(position) as DiaryEvent
                holder.bind(diaryItem)
            }
            is DiaryContentViewHolder -> {
                val diaryItems = getItem(position) as DiaryEvent
                holder.bind(diaryItems)
                holder.onclick.setOnClickListener {
                    editClickListener(diaryItems)
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
        fun bind(item: DiaryEvent) {
            binding.apply {
                val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(item.event_start)
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

        fun bind(item: DiaryEvent) {
            binding.apply {

                itemDiaryContentTv.text = item.content
                itemDiaryTitleTv.text = item.event_title

                setViewMore(itemDiaryContentTv, viewMore)

                val repo = DiaryRepository(context)

                val category =
                    repo.getCategory(item.event_category_idx, item.event_category_server_idx)

                context.resources?.let {
                    itemDiaryCategoryColorIv.background.setTint(category.color)
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
