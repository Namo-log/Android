package com.mongmong.namo.presentation.ui.home.schedule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.ItemDialogScheduleCategoryBinding

class DialogCategoryRVAdapter(
    private val categoryList: List<Category>
) : RecyclerView.Adapter<DialogCategoryRVAdapter.ViewHolder>() {

    private var selectedId : Long = 0

    interface MyItemClickListener {
        fun onSendId(category: Category)
    }

    private lateinit var mItemClickListener : MyItemClickListener

    fun setMyItemClickListener(itemClickListener : MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    fun setSelectedId(id : Long) {
        this.selectedId = id
    }

    inner class ViewHolder(val binding: ItemDialogScheduleCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category : Category) {
            binding.category = category
            if (category.categoryId == selectedId) {
                binding.categorySelectedIv.visibility = View.VISIBLE
            } else {
                binding.categorySelectedIv.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemDialogScheduleCategoryBinding = ItemDialogScheduleCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categoryList[position])

        holder.itemView.setOnClickListener {
            selectedId = categoryList[position].categoryId
            mItemClickListener.onSendId(categoryList[position])
        }
    }

    override fun getItemCount(): Int = categoryList.size
}