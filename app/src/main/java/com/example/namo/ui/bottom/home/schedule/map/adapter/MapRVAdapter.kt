package com.example.namo.ui.bottom.home.schedule.map.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.ui.bottom.home.schedule.map.data.Place
import com.example.namo.databinding.ItemMapPlaceBinding

class MapRVAdapter() : RecyclerView.Adapter<MapRVAdapter.ViewHolder>() {

    private val places = ArrayList<Place>()
    private lateinit var context : Context

    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    fun setItemClickListener(onItemClickListener : OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class ViewHolder(val binding : ItemMapPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place : Place) {
            binding.itemMapPlaceTitleTv.text = place.place_name
            binding.itemMapPlaceRoadAddressTv.text = place.road_address_name
            binding.itemMapPlaceOldAddressTv.text = place.address_name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemMapPlaceBinding = ItemMapPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(places[position])

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(position)
        }
    }

    override fun getItemCount(): Int = places.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPlaces(places : ArrayList<Place>) {
        this.places.clear()
        this.places.addAll(places)
    }
}