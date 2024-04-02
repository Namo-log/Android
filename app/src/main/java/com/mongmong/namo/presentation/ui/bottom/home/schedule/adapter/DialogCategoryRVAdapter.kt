package com.mongmong.namo.presentation.ui.bottom.home.schedule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.ItemDialogScheduleCategoryBinding
import com.mongmong.namo.presentation.config.CategoryColor

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
            binding.categoryColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)
            binding.categoryNameTv.text = category.name

            if (category.categoryId == selectedIdx) {
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
            selectedIdx = categoryList[position].categoryId
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