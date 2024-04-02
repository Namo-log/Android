package com.mongmong.namo.presentation.ui.bottom.custom.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemPaletteColorBinding

class PaletteColorRVAdapter(val context: Context, private val colors: ArrayList<String>) : RecyclerView.Adapter<PaletteColorRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPaletteColorBinding = ItemPaletteColorBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int = 10 // colors.size

    inner class ViewHolder(val binding: ItemPaletteColorBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(hexColor : String) {
            binding.itemPaletteColorCv.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hexColor))
        }
    }
}