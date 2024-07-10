package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryMoimActivityBinding
import com.mongmong.namo.domain.model.group.MoimActivity
import java.text.NumberFormat
import java.util.*

class MoimActivityRVAdapter(
    val payClickListener: (pay: Long, position: Int, payText: TextView) -> Unit,
    val imageDetailClickListener: () -> Unit,
    val updateImageClickListener: (position: Int) -> Unit,
    val activityNameTextWatcher: (text: String, position: Int) -> Unit,
    val deleteItemList: (deleteItems: MutableList<Long>) -> Unit,
    val deleteImageClickListener: (position: Int, image: String) -> Unit
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
            ItemDiaryMoimActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listData[position])

        with(holder.binding) {
            // 정산 클릭
            clickMoneyLy.setOnClickListener {
                payClickListener(listData[position].pay, position, itemPlaceMoneyTv)
            }

            // 활동 삭제 클릭
            activityDeleteLl.setOnClickListener {
                if (listData[position].moimActivityId != 0L) deleteItems.add(listData[position].moimActivityId)
                deleteItemList(deleteItems)
                listData.remove(listData[position])
                notifyDataSetChanged()
            }

            // 활동 이미지 수정
            updateImageIv.setOnClickListener {
                updateImageClickListener(position)
            }

            // 활동별 이미지 RecyclerView
            val adapter = MoimActivityGalleryAdapter(
                itemClickListener = imageDetailClickListener,
                deleteImageClickListener = { image ->
                    deleteImageClickListener(position, image)
                }
            )
            moimGalleryRv.apply {
                this.adapter = adapter.apply {}
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            listData[position].imgs?.let { adapter.addItem(it) }
        }

        holder.bind(listData[position])
    }

    override fun getItemCount(): Int = listData.size

    inner class ViewHolder(val binding: ItemDiaryMoimActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: MoimActivity) {
            binding.itemPlaceNameTv.setText(item.name)
            binding.itemPlaceNameTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    activityNameTextWatcher(p0.toString(), absoluteAdapterPosition)
                }
            })
            // 정산 다이얼로그
            binding.itemPlaceMoneyTv.text = NumberFormat.getNumberInstance(Locale.US).format(item.pay)


        }
    }
}

