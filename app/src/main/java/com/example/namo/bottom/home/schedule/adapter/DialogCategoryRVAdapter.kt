package com.example.namo.bottom.home.schedule.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.bottom.home.schedule.data.Category
import com.example.namo.databinding.ItemScheduleDialogCategoryBinding

class DialogCategoryRVAdapter : RecyclerView.Adapter<DialogCategoryRVAdapter.ViewHolder>() {

    private val categoryList = ArrayList<Category>()
    private lateinit var context : Context

    private var selectedPos : Int = 0

    interface MyItemClickListener {
        fun onSendPos(selected : Int)
    }

    private lateinit var mItemClickListener : MyItemClickListener

    fun setMyItemClickListener(itemClickListener : MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    inner class ViewHolder(val binding : ItemScheduleDialogCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category : Category) {
            binding.categoryColorView.background.setTint(context.resources.getColor(category.color))
            binding.categoryNameTv.text = category.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemScheduleDialogCategoryBinding = ItemScheduleDialogCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categoryList[position])

        if (position == selectedPos) {
            holder.binding.categorySelectedIv.visibility = View.VISIBLE
        } else {
            holder.binding.categorySelectedIv.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            selectedPos = holder.adapterPosition
            mItemClickListener.onSendPos(selectedPos)
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int = categoryList.size

    @SuppressLint("NotifyDataSetChanged")
    fun addCategory(categoryList : ArrayList<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)
    }

    fun setSelectedPos(pos : Int) {
        this.selectedPos = pos
    }

}