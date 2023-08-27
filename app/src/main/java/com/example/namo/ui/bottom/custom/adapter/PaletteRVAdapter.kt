package com.example.namo.ui.bottom.custom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemPaletteBinding
import com.example.namo.data.entity.custom.Palette

class PaletteRVAdapter(val context: Context) :  RecyclerView.Adapter<PaletteRVAdapter.ViewHolder>(){
    lateinit var items: ArrayList<Palette>

    fun build(i: ArrayList<Palette>): PaletteRVAdapter {
        items = i
        return this
    }

    class ViewHolder(val binding: ItemPaletteBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Palette) {
            with(binding)
            {
                itemPaletteNameTv.text = item.name
                itemPaletteColorRv.apply {
                    adapter = PaletteColorRVAdapter(context, item.colors) //컬러 어댑터 연결
                    layoutManager = GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemPaletteBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            parent.context
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

}