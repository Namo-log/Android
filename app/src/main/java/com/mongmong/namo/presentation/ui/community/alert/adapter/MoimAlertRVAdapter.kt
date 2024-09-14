package com.mongmong.namo.presentation.ui.community.alert.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimAlertBinding
import com.mongmong.namo.domain.model.Moim

class MoimAlertRVAdapter: RecyclerView.Adapter<MoimAlertRVAdapter.ViewHolder>(){

    private var moimRequestList = emptyList<Moim>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addRequest(moimList: List<Moim>) {
        this.moimRequestList = moimList
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onMoimInfoClick(position: Int)
        fun onAcceptBtnClick(position: Int)
        fun onDenyBtnClick(position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMoimAlertBinding = ItemMoimAlertBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(moimRequestList[position])
        holder.apply {
            // 수락 버튼 클릭
            binding.itemMoimAlertAcceptBtn.setOnClickListener {
                mItemClickListener.onAcceptBtnClick(position)
            }
            // 거절 버튼 클릭
            binding.itemMoimAlertDenyBtn.setOnClickListener {
                mItemClickListener.onDenyBtnClick(position)
            }
        }
    }

    override fun getItemCount(): Int = moimRequestList.size

    inner class ViewHolder(val binding: ItemMoimAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        //TODO: 실제 친구 요청 데이터로 변경
        fun bind(moim: Moim) {
            binding.moim = moim
        }
    }
}