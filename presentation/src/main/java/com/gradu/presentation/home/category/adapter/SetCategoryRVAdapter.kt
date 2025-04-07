package com.gradu.presentation.ui.home.category.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemCategoryBinding
import com.mongmong.namo.domain.model.CategoryModel

class SetCategoryRVAdapter:  RecyclerView.Adapter<SetCategoryRVAdapter.ViewHolder>(){

    private var categoryList = ArrayList<CategoryModel>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setCategoryClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addCategory(categories: ArrayList<CategoryModel>) {
        this.categoryList = categories
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onItemClick(category: CategoryModel, position: Int)
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

        fun bind(category : CategoryModel) {
            binding.category = category
        }
    }

}