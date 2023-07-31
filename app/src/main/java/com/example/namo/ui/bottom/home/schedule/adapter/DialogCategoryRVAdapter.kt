package com.example.namo.ui.bottom.home.schedule.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.home.Category
import com.example.namo.databinding.ItemDialogScheduleCategoryBinding

class DialogCategoryRVAdapter(
    var context: Context,
    private val categoryList: List<Category>
) : RecyclerView.Adapter<DialogCategoryRVAdapter.ViewHolder>() {

    private var selectedIdx : Long = 0

    interface MyItemClickListener {
        fun onSendIdx(category: Category)
    }

    private lateinit var mItemClickListener : MyItemClickListener

    fun setMyItemClickListener(itemClickListener : MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    inner class ViewHolder(val binding : ItemDialogScheduleCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category : Category) {
            binding.categoryColorView.background.setTint(context.resources.getColor(category.color))
            binding.categoryNameTv.text = category.name

            if (category.categoryIdx == selectedIdx) {
                binding.categorySelectedIv.visibility = View.VISIBLE
            } else {
                binding.categorySelectedIv.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemDialogScheduleCategoryBinding = ItemDialogScheduleCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categoryList[position])

        holder.itemView.setOnClickListener {
            selectedIdx = categoryList[position].categoryIdx
            mItemClickListener.onSendIdx(categoryList[position])

            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int = categoryList.size

    fun setSelectedIdx(idx : Long) {
        this.selectedIdx = idx
        notifyDataSetChanged()
    }

}