package com.example.namo.ui.bottom.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.databinding.ItemDiaryListBinding
import java.text.SimpleDateFormat

class DiaryDateListAdapter (
    val context: Context,
    var list: List<Event>
):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val date_list=1  // 날짜
    val content_list=2  // 날짜 별 내용
    private lateinit var datelist:ItemDiaryDateListBinding
    private lateinit var contentlist:ItemDiaryListBinding

    @SuppressLint("ResourceType")
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        return when(viewType) {
            date_list -> {
               datelist = ItemDiaryDateListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
               dateHolder(datelist)
            }
            content_list -> {
                contentlist = ItemDiaryListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                contentsHolder(contentlist)
            }
            else->{
                throw java.lang.RuntimeException("Error")
            } }
        }

    override fun getItemCount(): Int =list.size


    override fun getItemViewType(position: Int): Int {
        return list[position].order
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is dateHolder){
            holder.bind(list[position])
        }
        else if (holder is contentsHolder){
           holder.bind(list[position])
        }
    }

    inner class dateHolder(val binding:ItemDiaryDateListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:Event){
            val formattedDate= SimpleDateFormat("yyyy.MM.dd").format(item.startLong)
            binding.diaryDayTv.text= formattedDate
        }
    }
    inner class contentsHolder(val binding:ItemDiaryListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:Event){
            binding.itemDiaryContentTv.text
            binding.itemDiaryContentTv.text=item.content
            binding.itemDiaryTitleTv.text=item.title
            binding.itemDiaryCategoryColorIv.background.setTint(ContextCompat.getColor(context,item.categoryColor))
            binding.diaryGalleryRv.adapter= DiaryGalleryRVAdapter(context, item.imgs)
            binding.diaryGalleryRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            if(item.content.isEmpty()) binding.itemDiaryContentTv.visibility= View.GONE
            if(item.imgs?.isEmpty() == true) binding.diaryGalleryRv.visibility=View.GONE
        }
    }

}



//class DiaryDateListAdapter (
//    val context: Context,
//    private val date:List<Long>,
//    var list: List<DiaryList>
//):
//    RecyclerView.Adapter<DiaryDateListAdapter.ViewHolder>() {
//
//
//    interface DiaryDateInterface {
//        fun onDate(date: Long)
//    }
//    private lateinit var diaryDate: DiaryDateInterface
//    fun setDate(item: DiaryDateListAdapter.DiaryDateInterface){
//        diaryDate=item
//    }
//
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        val binding: ItemDiaryDateListBinding = ItemDiaryDateListBinding.inflate(
//            LayoutInflater.from(viewGroup.context), viewGroup, false
//        )
//        return ViewHolder(binding)
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        holder.bind(date[position])
//
//        val r=Runnable {
//            try {
//                diaryDate.onDate(date[position])
//            }catch (e: Exception) {
//                Log.d("tag", "Error - $e")
//            }
//        }
//        val thread=Thread(r)
//        thread.start()
//    }
//
//    override fun getItemCount(): Int =date.size
//
//    override fun getItemViewType(position: Int): Int {
//        return position
//    }
//
//    inner class ViewHolder(val binding: ItemDiaryDateListBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
//        fun bind(date: Long) {
//
//            val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(date)
//            binding.diaryDayTv.text = formattedDate
//
//            binding.diaryEventRv.adapter = DiaryListRVAdapter(context, list)
//            binding.diaryEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//            //binding.diaryEventRv.setHasFixedSize(true)
//
//            val view=View(context)
//            val diarydateAdatper=DiaryListRVAdapter(context, list)
//            diarydateAdatper.setRecordClickListener(object : DiaryListRVAdapter.DiaryEditInterface{
//                override fun onEditClicked(allData: DiaryList) {
//                    val bundle= Bundle()
//                    bundle.putInt("scheduleIdx",allData.eventId)
//
//                    val diaryFrag= DiaryModifyFragment()
//                    bundle.also { diaryFrag.arguments = it }
//
//                    view.findNavController().navigate(R.id.action_diaryFragment_to_diaryModifyFragment)
//                }
//            })
//        }
//
//    }
//}