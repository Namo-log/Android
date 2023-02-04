package com.example.namo.ui.bottom.custom.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemCustomPaletteColorBinding

class PaletteColorRVAdapter : RecyclerView.Adapter<PaletteColorRVAdapter.ViewHolder>() {

    lateinit var items: ArrayList<String>

    fun build(i: ArrayList<String>): PaletteColorRVAdapter {
        items = i
        return this
    }

    class ViewHolder(val binding: ItemCustomPaletteColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.itemPaletteColorCv.setCardBackgroundColor(Color.parseColor(item))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCustomPaletteColorBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = 10 //items.size
}