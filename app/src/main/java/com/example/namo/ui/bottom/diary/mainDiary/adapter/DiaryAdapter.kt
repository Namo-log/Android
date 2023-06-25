import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import com.example.namo.ui.bottom.diary.mainDiary.ImageDialog
import com.example.namo.ui.bottom.diary.mainDiary.adapter.DiaryGalleryRVAdapter
import java.text.SimpleDateFormat

class DiaryAdapter(
    private val fragmentManagers: FragmentManager,
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
                itemDiaryTitleTv.text = item.title
                itemDiaryCategoryColorIv.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        item.categoryColor
                    )
                )
                val adapter = DiaryGalleryRVAdapter(itemView.context, item.imgs)
                diaryGalleryRv.adapter = adapter
                diaryGalleryRv.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

                adapter.setImageClickListener(object : DiaryGalleryRVAdapter.DiaryImageInterface {
                    override fun onImageClicked(image: String) {
                        ImageDialog(image).show(fragmentManagers, "test")
                    }
                })

                if (item.content.isEmpty()) itemDiaryContentTv.visibility = View.GONE
                if (item.imgs?.isEmpty() == true) diaryGalleryRv.visibility = View.GONE
            }
        }
    }
}

