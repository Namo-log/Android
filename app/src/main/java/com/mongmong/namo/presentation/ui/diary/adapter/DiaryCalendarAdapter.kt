import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ItemDiaryCalendarBinding

data class CalendarDay(val date: String, val year: Int, val month: Int)

class DiaryCalendarAdapter(
    private val items: List<CalendarDay>,
    private val listener: OnCalendarDayClickListener
) : RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    private var isBottomSheetOpened: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        // 상태 초기화
        holder.binding.itemDiaryCalendarRootMl.setTransition(R.id.start, R.id.end)
        holder.binding.itemDiaryCalendarRootMl.progress = 0f

        holder.updateItemView(isBottomSheetOpened)
    }

    override fun getItemCount(): Int = items.size

    fun updateAllItems(isOpened: Boolean) {
        this.isBottomSheetOpened = isOpened
        notifyDataSetChanged()  // 모든 아이템의 뷰를 업데이트
    }

    inner class ViewHolder(val binding: ItemDiaryCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay) {
            binding.diaryCalendarDateTv.text = calendarDay.date

            binding.root.setOnClickListener {
                listener.onCalendarDayClick(calendarDay)
            }
        }

        fun updateItemView(isBottomSheetOpened: Boolean) {
            val motionLayout = binding.itemDiaryCalendarRootMl
            if (isBottomSheetOpened) {
                motionLayout.setTransition(R.id.start, R.id.end)
            } else {
                motionLayout.setTransition(R.id.end, R.id.start)
            }
        }

    }

    interface OnCalendarDayClickListener {
        fun onCalendarDayClick(calendarDay: CalendarDay)
    }
}
