package com.mongmong.namo.presentation.ui.home.category.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemCategoryBinding
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.presentation.config.CategoryColor

class SetCategoryRVAdapter:  RecyclerView.Adapter<SetCategoryRVAdapter.ViewHolder>(){

    private val categoryList = ArrayList<Category>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setCategoryClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addCategory(categories: ArrayList<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categories)
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onItemClick(category: Category, position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCategoryBinding = ItemCategoryBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categoryList[position])
        holder.apply {
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(categoryList[position], position)
            }
        }
    }

    override fun getItemCount(): Int = categoryList.size

    inner class ViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(category : Category) {
            binding.itemCategoryColorIv.background.setTint(Color.parseColor(CategoryColor.convertPaletteIdToHexColor(category.paletteId)))
            binding.itemCategoryNameTv.text = category.name
        }

//        val name : TextView = binding.itemCategoryNameTv
//        val color = binding.itemCategoryColorIv
    }

}