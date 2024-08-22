package com.mongmong.namo.presentation.ui.community.moim.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimBinding
import com.mongmong.namo.domain.model.Moim

class MoimRVAdapter: RecyclerView.Adapter<MoimRVAdapter.ViewHolder>(){

    private var moimList = emptyList<Moim>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addMoim(moimList: List<Moim>) {
        this.moimList = moimList
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
        holder.bind(moimList[position])
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

    override fun getItemCount(): Int = moimList.size

    inner class ViewHolder(val binding: ItemMoimBinding) : RecyclerView.ViewHolder(binding.root) {
        //TODO: 실제 모임 일정 데이터로 변경
        fun bind(moim: Moim) {
            binding.moim = moim
        }
    }
}