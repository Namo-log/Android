package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryMoimActivityBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.group.MoimActivity
import java.text.NumberFormat
import java.util.*

class MoimActivityRVAdapter(
    val payClickListener: (pay: Long, position: Int, payText: TextView) -> Unit,
    val imageDetailClickListener: (position: Int) -> Unit,
    val updateImageClickListener: (position: Int) -> Unit,
    val activityNameTextWatcher: (text: String, position: Int) -> Unit,
    val deleteActivityClickListener: (activityId: Long) -> Unit,
    val deleteImageClickListener: (position: Int, image: DiaryImage) -> Unit
) : RecyclerView.Adapter<MoimActivityRVAdapter.ViewHolder>() {

    private val listData = mutableListOf<MoimActivity>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newActivities: List<MoimActivity>) {
        listData.clear()
        listData.addAll(newActivities)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryMoimActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 뷰 상태 초기화
        holder.binding.activityLayout.translationX = 0f
        holder.bind(listData[position])
        with(holder.binding) {
            clickMoneyLy.setOnClickListener {
                payClickListener(listData[position].pay, position, itemPlaceMoneyTv)
            }

            activityDeleteLl.setOnClickListener {
                val activityId = listData[position].moimActivityId
                Log.d("activityDeleteLl", "$activityId, $position")
                deleteActivityClickListener(activityId)
            }

            updateImageIv.setOnClickListener {
                updateImageClickListener(position)
            }

            val adapter = MoimActivityGalleryAdapter(
                itemClickListener = { imageDetailClickListener(position) },
                deleteImageClickListener = { diaryImage ->
                    deleteImageClickListener(position, diaryImage)
                }
            )
            moimGalleryRv.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            listData[position].images?.let { adapter.addItem(it) }
        }
    }


    override fun getItemCount(): Int = listData.size

    inner class ViewHolder(val binding: ItemDiaryMoimActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MoimActivity) {
            binding.itemPlaceNameTv.setText(item.name)
            binding.itemPlaceNameTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    activityNameTextWatcher(p0.toString(), absoluteAdapterPosition)
                }
            })
            binding.itemPlaceMoneyTv.text = NumberFormat.getNumberInstance(Locale.US).format(item.pay)
        }
    }
}
