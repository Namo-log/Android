package com.example.namo.ui.bottom.group.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.namo.R
import com.example.namo.data.remote.moim.Moim
import com.example.namo.databinding.ItemGroupListBinding

class GroupListRVAdapter(private val moimList: List<Moim>):  RecyclerView.Adapter<GroupListRVAdapter.ViewHolder>() {

    private lateinit var context : Context

    interface ItemClickListener{
        fun onItemClick(moim : Moim)
    }

    private lateinit var mItemClickListener : ItemClickListener

    fun setMyItemClickListener (itemClickListener : ItemClickListener){
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGroupListBinding = ItemGroupListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(moimList[position])

        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(moimList[position])
        }
    }

    override fun getItemCount(): Int = moimList.size

    inner class ViewHolder(val binding: ItemGroupListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(moim: Moim) {
            binding.itemGroupTitleTv.text = moim.groupName

            Glide.with(context)
                .load(moim.groupImgUrl)
                .placeholder(R.drawable.app_logo_namo)
                .error(R.drawable.app_logo_namo)
                .fallback(R.drawable.app_logo_namo)
                .circleCrop()
                .into(binding.itemGroupCoverImgIv)

            binding.itemGroupTotalPeopleNumTv.text = moim.moimUsers.size.toString()

            val userNameList: List<String> = moim.moimUsers.map { it.userName }
            binding.itemGroupTotalPeopleNameTv.text = userNameList.joinToString()
        }
    }
}