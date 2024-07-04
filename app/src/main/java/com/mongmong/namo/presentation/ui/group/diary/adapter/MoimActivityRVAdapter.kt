package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryGroupEventBinding
import com.mongmong.namo.domain.model.group.MoimActivity
import java.text.NumberFormat
import java.util.*

class MoimActivityRVAdapter(
    private val context: Context,
    val payClickListener: (pay: Long, position: Int, payText: TextView) -> Unit,
    val imageClickListener: (position: Int) -> Unit,
    val activityClickListener: (text: String, position: Int) -> Unit,
    val deleteItemList: (deleteItems: MutableList<Long>) -> Unit
) : RecyclerView.Adapter<MoimActivityRVAdapter.ViewHolder>() {

    private val listData = mutableListOf<MoimActivity>()
    private val deleteItems = mutableListOf<Long>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newActivities: List<MoimActivity>) {
        listData.clear()
        listData.addAll(newActivities)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDiaryGroupEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val updatedPosition = holder.bindingAdapterPosition

        with(holder.binding) {
            // 정산 다이얼로그
            itemPlaceMoneyTv.text = NumberFormat.getNumberInstance(Locale.US).format(listData[position].pay)
            clickMoneyLy.setOnClickListener {
                payClickListener(listData[position].pay, updatedPosition, holder.binding.itemPlaceMoneyTv)
            }

            // 장소별 이미지 가져오기
            val adapter = MoimActivityGalleryAdapter(context)
            groupAddGalleryRv.apply {
                this.adapter = adapter.apply {
                    itemClickListener = if (listData[position].imgs?.size == 3) {
                        { imageClickListener(updatedPosition) }
                    } else {
                        null
                    }
                }
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            if (listData[position].imgs?.size == 3) {
                img1.visibility = View.GONE
                img2.visibility = View.GONE
                img3.visibility = View.GONE
            } else if (listData[position].imgs?.isNotEmpty() == true) {
                img1.visibility = View.VISIBLE
                img2.visibility = View.GONE
                img3.visibility = View.GONE
            } else {
                img1.visibility = View.VISIBLE
                img2.visibility = View.VISIBLE
                img3.visibility = View.VISIBLE
            }

            holder.binding.groupGalleryLv.setOnClickListener {
                imageClickListener(updatedPosition)
            }

            holder.binding.itemPlaceNameTv.hint = "활동"
            listData[position].imgs?.let { adapter.addItem(it) }
        }

        holder.bind(listData[position])
    }

    override fun getItemCount(): Int = listData.size

    inner class ViewHolder(val binding: ItemDiaryGroupEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: MoimActivity) {
            binding.itemPlaceNameTv.setText(item.name)

            binding.itemPlaceNameTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    activityClickListener(p0.toString(), absoluteAdapterPosition)
                }
            })

            binding.onclickDeleteItem.setOnClickListener {
                if (item.moimActivityId != 0L) deleteItems.add(item.moimActivityId)
                deleteItemList(deleteItems)
                listData.remove(item)
                notifyDataSetChanged()
            }
        }
    }
}

