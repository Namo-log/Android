package com.example.namo.ui.bottom.diary.groupDiary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.databinding.ItemDiaryGroupEventBinding

class GroupModifyRVAdapter(  // 그룹 다이어리 수정
    val context: Context,
    initialItems: List<DiaryGroupEvent> = emptyList()
) :
    RecyclerView.Adapter<GroupModifyRVAdapter.ViewHolder>(),
    ItemTouchHelperListener {

    private val items = ArrayList<DiaryGroupEvent>(initialItems)

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: List<DiaryGroupEvent>) {
        this.items.clear()
        this.items.addAll(image)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupEventBinding = ItemDiaryGroupEventBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])

        val adapter = GroupPlaceGalleryAdapter(context)
        holder.binding.groupAddGalleryRv.adapter = adapter
        holder.binding.groupAddGalleryRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        items[position].imgs?.let { adapter.addItem(it) }

    }


    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemDiaryGroupEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiaryGroupEvent) {
            binding.itemPlaceNameTv.setText(item.place)
            binding.itemPlaceMoneyTv.text = item.pay.toString()

            binding.groupGalleryLv.visibility = View.GONE
            binding.groupAddGalleryRv.visibility = View.VISIBLE

            binding.itemPlaceMoneyIv.visibility=View.GONE
        }
    }

    override fun onItemSwipe(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}