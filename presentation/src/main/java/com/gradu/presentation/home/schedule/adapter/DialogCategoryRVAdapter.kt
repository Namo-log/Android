package com.gradu.presentation.ui.home.schedule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.CategoryModel
import com.mongmong.namo.databinding.ItemDialogScheduleCategoryBinding

class DialogCategoryRVAdapter(
    private val categoryList: List<CategoryModel>
) : RecyclerView.Adapter<DialogCategoryRVAdapter.ViewHolder>() {

    private var selectedId : Long = 0

    interface MyItemClickListener {
        fun onSelectCategory(category: CategoryModel)
        fun onEditCategory(category: CategoryModel)
    }

    private lateinit var mItemClickListener : MyItemClickListener

    fun setMyItemClickListener(itemClickListener : MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    fun setSelectedId(id : Long) {
        this.selectedId = id
    }

    inner class ViewHolder(val binding: ItemDialogScheduleCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category : CategoryModel) {
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

        // 화면 이동
        holder.binding.apply {
            // 카테고리 선택
            categoryColorView.setOnClickListener {
                selectedId = categoryList[position].categoryId
                mItemClickListener.onSelectCategory(categoryList[position]) // 카테고리 선택 진행
            }

            // 카테고리 수정
            categoryEditLl.setOnClickListener {
                mItemClickListener.onEditCategory(categoryList[position])
            }
        }
    }

    override fun getItemCount(): Int = categoryList.size
}