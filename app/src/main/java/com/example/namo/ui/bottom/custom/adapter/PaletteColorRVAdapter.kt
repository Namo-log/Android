package com.example.namo.ui.bottom.custom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemPaletteColorBinding

class PaletteColorRVAdapter(val context: Context, private val colors: ArrayList<Int>) : RecyclerView.Adapter<PaletteColorRVAdapter.ViewHolder>() {

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

        fun bind(color : Int) {
            binding.itemPaletteColorCv.background.setTint(context.resources.getColor(color))
        }
    }
}