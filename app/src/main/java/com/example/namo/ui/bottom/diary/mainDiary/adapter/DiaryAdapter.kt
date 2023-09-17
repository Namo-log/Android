import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGalleryRVAdapter
import java.text.SimpleDateFormat

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class DiaryAdapter( // 월 별 개인 다이어리 리스트 어댑터
    val editClickListener: (DiaryItem.Content) -> Unit,
    val imageClickListener: (String) -> Unit
) : ListAdapter<DiaryItem, RecyclerView.ViewHolder>(DiaryDiffCallback()) {


    fun updateData(newData: List<DiaryItem>) {
        submitList(newData)
    }

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryItem>() {
        override fun areItemsTheSame(oldItem: DiaryItem, newItem: DiaryItem): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiaryItem, newItem: DiaryItem): Boolean {
            return oldItem == newItem
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DiaryHeaderViewHolder -> {
                val diaryItem = getItem(position) as DiaryItem.Header
                holder.bind(diaryItem)
            }
            is DiaryContentViewHolder -> {
                val diaryItems = getItem(position) as DiaryItem.Content
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
        return when (getItem(position)) {
            is DiaryItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DiaryItem.Content -> ITEM_VIEW_TYPE_ITEM
            else -> throw ClassCastException("Unknown viewType $position")
        }
    }

    class DiaryHeaderViewHolder
    private constructor(private val binding: ItemDiaryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(item: DiaryItem.Header) {
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
        private val binding: ItemDiaryDateListBinding,
        private val context: Context,
        private val imageClickListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        val onclick = binding.diaryEditTv

        fun bind(item: DiaryItem.Content) {
            binding.apply {

                itemDiaryContentTv.text = item.content
                itemDiaryTitleTv.text = item.event_title

                setViewMore(itemDiaryContentTv, viewMore)

                val repo = DiaryRepository(context)

                val category =
                    repo.getCategory(item.event_category_idx, item.event_category_server_idx)

                context.resources?.let {
                    binding.itemDiaryCategoryColorIv.background.setTint(category.color)
                }

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
                val binding = ItemDiaryDateListBinding.inflate(layoutInflater, parent, false)
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

sealed class DiaryItem {
    abstract val id: Long

    data class Header(override val id: Long, val date: Long) : DiaryItem()

    data class Content(

        var eventId: Long = 0L,
        var event_title: String = "",
        var event_start: Long = 0L,
        var event_category_idx: Long = 0L,
        var event_place_name: String = "없음",
        var content: String?,
        var images: List<String>? = null,
        var event_server_idx: Long = 0L,
        var event_category_server_idx: Long = 0L,
        override val id: Long

    ) : DiaryItem(),java.io.Serializable


}

