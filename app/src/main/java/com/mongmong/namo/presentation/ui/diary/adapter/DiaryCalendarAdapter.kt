import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryCalendarBinding

data class CalendarDay(val date: String, val year: Int, val month: Int)

class DiaryCalendarAdapter(private val calendarItems: List<CalendarDay>) :
    RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    var itemHeightMultiplier = 8

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val itemHeight = parent.measuredHeight / itemHeightMultiplier
        val itemWidth = parent.measuredWidth / 7

        val layoutParams = binding.root.layoutParams
        layoutParams.height = itemHeight
        layoutParams.width = itemWidth
        binding.root.layoutParams = layoutParams

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = calendarItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = calendarItems.size

    fun getItem(position: Int): CalendarDay = calendarItems[position]

    inner class ViewHolder(private val binding: ItemDiaryCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDay) {
            binding.diaryCalendarDateTv.text = item.date
        }
    }
}
