package com.mongmong.namo.presentation.ui.community.moim.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimBinding
import com.mongmong.namo.domain.model.MoimPreview

class MoimRVAdapter: RecyclerView.Adapter<MoimRVAdapter.ViewHolder>(){

    private var moimPreviewList = emptyList<MoimPreview>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addMoim(moimPreviewList: List<MoimPreview>) {
        this.moimPreviewList = moimPreviewList
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onRecordButtonClick(position: Int)
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMoimBinding = ItemMoimBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(moimPreviewList[position])
        holder.apply {
            // 아이템 전체 클릭
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(position)
            }
            // 기록 버튼 클릭
            binding.itemMoimRecordIv.setOnClickListener {
                mItemClickListener.onRecordButtonClick(position)
            }
        }
    }

    override fun getItemCount(): Int = moimPreviewList.size

    inner class ViewHolder(val binding: ItemMoimBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(moimPreview: MoimPreview) {
            binding.moim = moimPreview
        }
    }
}