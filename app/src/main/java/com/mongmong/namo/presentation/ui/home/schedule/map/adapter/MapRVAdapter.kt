package com.mongmong.namo.presentation.ui.home.schedule.map.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.presentation.ui.home.schedule.map.data.Place
import com.mongmong.namo.databinding.ItemMapPlaceBinding

class MapRVAdapter : RecyclerView.Adapter<MapRVAdapter.ViewHolder>() {

    private val places = ArrayList<Place>()
    private lateinit var context : Context

    private lateinit var itemClickListener : OnItemClickListener
    private var selectedPosition = RecyclerView.NO_POSITION // 선택된 아이템의 포지션을 저장

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    fun setItemClickListener(onItemClickListener : OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addPlaces(places : ArrayList<Place>) {
        this.places.clear()
        this.places.addAll(places)
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val prevPosition = selectedPosition
        selectedPosition = position
        if (prevPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(prevPosition)
            return
        }
        notifyItemChanged(position)
    }

    inner class ViewHolder(val binding : ItemMapPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place : Place, isSelected: Boolean) {
            binding.itemMapPlaceTitleTv.text = place.place_name
            binding.itemMapPlaceRoadAddressTv.text = place.road_address_name
            binding.itemMapPlaceOldAddressTv.text = place.address_name
            // 체크 표시 업데이트
            binding.itemMapPlaceSelectIv.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemMapPlaceBinding = ItemMapPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isSelected = position == selectedPosition
        holder.bind(places[position], isSelected)

        holder.itemView.setOnClickListener {
            // 클릭된 아이템 포지션을 업데이트하고 노티파이
            val previousPosition = selectedPosition
            selectedPosition = holder.absoluteAdapterPosition
            notifyItemChanged(previousPosition) // 이전 선택된 아이템 업데이트
            notifyItemChanged(selectedPosition) // 현재 선택된 아이템 업데이트
            itemClickListener.onClick(position)
        }
    }

    override fun getItemCount(): Int = places.size
}