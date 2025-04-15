package com.gradu.presentation.community.moim.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gradu.presentation.databinding.ItemMoimBinding

class MoimRVAdapter: RecyclerView.Adapter<MoimRVAdapter.ViewHolder>(){

    private var moimPreviewList = emptyList<com.gradu.domain.model.MoimPreview>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addMoim(moimPreviewList: List<com.gradu.domain.model.MoimPreview>) {
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
        fun bind(moimPreview: com.gradu.domain.model.MoimPreview) {
            binding.moim = moimPreview
        }
    }
}