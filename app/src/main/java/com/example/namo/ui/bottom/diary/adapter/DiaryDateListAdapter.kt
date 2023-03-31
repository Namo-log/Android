package com.example.namo.ui.bottom.diary.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.persistableBundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.ItemDiaryDateListBinding
import com.example.namo.ui.bottom.diary.DiaryModifyFragment
import java.text.SimpleDateFormat


class DiaryDateListAdapter (
    val context: Context,
    private val date:List<Long>
):
    RecyclerView.Adapter<DiaryDateListAdapter.ViewHolder>() {
    lateinit var contents:List<Event>
    private lateinit var db: NamoDatabase

//    interface DiaryDateInterface {
//        fun onDate(date: Long) }
//
//    private lateinit var diaryDate: DiaryDateInterface
//    fun setDate(item: DiaryDateInterface) {
//        diaryDate = item
//    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryDateListBinding = ItemDiaryDateListBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        db=NamoDatabase.getInstance(context)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(date[position])

    }

    override fun getItemCount(): Int = date.size

    inner class ViewHolder(val binding: ItemDiaryDateListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
        fun bind(datetime: Long) {

            val view=View(context)
            val formattedDate = SimpleDateFormat("yyyy.MM.dd").format(datetime)
            binding.diaryDayTv.text = formattedDate

            val r=Runnable {
                try {
                    contents=db.diaryDao.getDateList(datetime)
                    val diarydateAdapter=DiaryListRVAdapter(context,contents)

                    binding.diaryEventRv.adapter=diarydateAdapter
                    diarydateAdapter.notifyDataSetChanged()

                    diarydateAdapter.setRecordClickListener(object : DiaryListRVAdapter.DiaryEditInterface{
                        override fun onEditClicked(allData: Event) {
                            val bundle= Bundle()
                            bundle.putInt("scheduleIdx",allData.eventId)

                            val diaryFrag= DiaryModifyFragment()
                            bundle.also { diaryFrag.arguments = it }

                            view.findNavController().navigate(R.id.diaryModifyFragment,bundle)
                        }
                    })

                }catch (e: Exception) {
                    Log.d("tag", "Error - $e")
                }
            }
            val thread=Thread(r)
            thread.start()

            binding.diaryEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        }


        }
    }
