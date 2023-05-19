package com.example.namo.ui.bottom.home.schedule.category.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemPaletteColorBinding

class CategoryPaletteRVAdapter(
    val context: Context,
    private val colorList: ArrayList<Int>
    ): RecyclerView.Adapter<CategoryPaletteRVAdapter.ViewHolder>() {

    private lateinit var mItemClickListener: MyItemClickListener

    fun setCategoryClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    interface MyItemClickListener {
        fun onItemClick(color: Int, position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPaletteColorBinding = ItemPaletteColorBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(colorList[position])
        holder.apply {
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(colorList[position], position)
            }
        }
    }

    override fun getItemCount(): Int = colorList.size

    inner class ViewHolder(val binding: ItemPaletteColorBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(color : Int) {
            binding.itemPaletteColorCv.background.setTint(context.resources.getColor(color))
        }
    }
}