package com.example.namo.ui.bottom.home.category.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemCategoryBinding
import com.example.namo.data.entity.home.Category

class SetCategoryRVAdapter(
    val context: Context,
    private val categoryList: List<Category>
):  RecyclerView.Adapter<SetCategoryRVAdapter.ViewHolder>(){

    private lateinit var mItemClickListener: MyItemClickListener

    fun setCategoryClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
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
            binding.itemCategoryColorIv.background.setTint(category.color)
            binding.itemCategoryNameTv.text = category.name
        }

//        val name : TextView = binding.itemCategoryNameTv
//        val color = binding.itemCategoryColorIv
    }

}