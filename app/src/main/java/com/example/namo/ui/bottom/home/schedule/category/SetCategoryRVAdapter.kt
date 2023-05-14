package com.example.namo.ui.bottom.home.schedule.category

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemCategoryBinding
import com.example.namo.ui.bottom.home.schedule.data.Category

class SetCategoryRVAdapter(
    val context: Context,
    val categoryList: List<Category>
):  RecyclerView.Adapter<SetCategoryRVAdapter.ViewHolder>(){

//    private val categoryList = ArrayList<Category>()

    private lateinit var mItemClickListener: MyItemClickListener

    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
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
//        if (result[position].color == "" || result[position].color == null) {
//        } else {
//            holder.color.setCardBackgroundColor(Color.parseColor(result[position].color))
//        }


//        holder.itemView.setOnClickListener{ mItemClickListener.onItemClick(result[position], position) }

    }

    override fun getItemCount(): Int = categoryList.size

    inner class ViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(category : Category) {
            binding.itemCategoryColorIv.background.setTint(context.resources.getColor(category.color))
            binding.itemCategoryNameTv.text = category.name
        }

        val name : TextView = binding.itemCategoryNameTv
        val color = binding.itemCategoryColorIv
    }

}