import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import com.example.namo.ui.bottom.diary.mainDiary.ImageDialog
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGalleryRVAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class DiaryAdapter(
    private val fragmentManagers: FragmentManager,
    val context: Context,
    initialItems: List<DiaryItem> = emptyList(),

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = ArrayList<DiaryItem>(initialItems)

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(items: List<DiaryItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryEditInterface {
        fun onEditClicked(allData: DiaryItem.Content)
    }

    private lateinit var diaryRecordClickListener: DiaryEditInterface
    fun setRecordClickListener(itemClickListener: DiaryEditInterface) {
        diaryRecordClickListener = itemClickListener
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_diary_list -> {
                val binding = ItemDiaryListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                DiaryHeaderViewHolder(binding)
            }

            R.layout.item_diary_date_list -> {
                val binding = ItemDiaryDateListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                DiaryContentVewHolder(binding)
            }
            else -> throw IllegalArgumentException("Cannot find ViewHolder for viewType : $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is DiaryHeaderViewHolder -> holder.bind(item as DiaryItem.Header)
            is DiaryContentVewHolder -> {
                holder.bind(item as DiaryItem.Content)
                holder.onclick.setOnClickListener {
                    diaryRecordClickListener.onEditClicked(item)
                }
            }
        }
    }

    inner class DiaryHeaderViewHolder(
        private val binding: ItemDiaryListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(item: DiaryItem.Header) {
            binding.apply {
                val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(item.date)
                diaryDayTv.text = formattedDate
            }
        }
    }

    inner class DiaryContentVewHolder(
        val binding: ItemDiaryDateListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val onclick = binding.diaryEditTv

        fun bind(item: DiaryItem.Content) {
            binding.apply {
                itemDiaryContentTv.text
                itemDiaryContentTv.text = item.content
                itemDiaryTitleTv.text = item.event_title

                setViewMore(itemDiaryContentTv, viewMore)

                val repo = DiaryRepository(context)
                CoroutineScope(Dispatchers.Main).launch {
                    
                    val category = repo.getCategoryId(item.event_category_idx)

                    context.resources?.let {
                        itemDiaryCategoryColorIv.background.setTint(
                            ContextCompat.getColor(
                                context,
                                category.color
                            )
                        )
                    }
                }

                val adapter = DiaryGalleryRVAdapter(itemView.context, item.images)
                diaryGalleryRv.adapter = adapter
                diaryGalleryRv.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

                adapter.setImageClickListener(object : DiaryGalleryRVAdapter.DiaryImageInterface {
                    override fun onImageClicked(image: String) {
                        ImageDialog(image).show(fragmentManagers, "test")
                    }
                })

                if (item.content?.isEmpty() == true) itemDiaryContentTv.visibility = View.GONE
                if (item.images?.isEmpty() == true) diaryGalleryRv.visibility = View.GONE
            }
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

