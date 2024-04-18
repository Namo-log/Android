package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryGroupEventBinding
import com.mongmong.namo.domain.model.group.MoimActivity
import java.text.NumberFormat
import java.util.*


class MoimActivityRVAdapter(
    // 그룹 다이어리 장소 추가, 정산, 이미지
    val context: Context,
    private val listData: MutableList<MoimActivity>,
    val payClickListener: (pay: Long, position: Int, payText: TextView) -> Unit,
    val imageClickListener: (imgLists: List<String>?, position: Int) -> Unit,
    val placeClickListener: (text: String, position: Int) -> Unit,
    val deleteItemList: (deleteItems: MutableList<Long>) -> Unit
) : RecyclerView.Adapter<MoimActivityRVAdapter.Holder>() {

    private val items = arrayListOf<ArrayList<String>?>()
    private var deleteItems = arrayListOf<Long>()

    @SuppressLint("NotifyDataSetChanged")
    fun addImageItem(image: ArrayList<String>?) {
        this.items.add(image)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ItemDiaryGroupEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {

        val event = listData[position]
        val updatedPosition = holder.bindingAdapterPosition

        // 정산 다이얼로그
        holder.binding.itemPlaceMoneyTv.text =
            NumberFormat.getNumberInstance(Locale.US).format(event.pay)
        holder.binding.clickMoneyLy.setOnClickListener {
            payClickListener(event.pay, updatedPosition, holder.binding.itemPlaceMoneyTv)
        }

        // 장소별 이미지 가져오기
        val adapter = GroupPlaceGalleryAdapter(context)
        holder.binding.groupAddGalleryRv.adapter = adapter
        holder.binding.groupAddGalleryRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        if (event.imgs?.isNotEmpty() == true) {
            holder.binding.img2.visibility = View.GONE
            holder.binding.img3.visibility = View.GONE
        } else {
            holder.binding.img2.visibility = View.VISIBLE
            holder.binding.img3.visibility = View.VISIBLE
        }

        holder.binding.groupGalleryLv.setOnClickListener {
            imageClickListener(event.imgs, updatedPosition)
        }

        holder.binding.itemPlaceNameTv.hint = "장소"
        event.imgs?.let { adapter.addItem(it) }

        holder.bind(event)

    }

    override fun getItemCount(): Int = listData.size


    inner class Holder(val binding: ItemDiaryGroupEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: MoimActivity) {

            binding.itemPlaceNameTv.setText(item.place)

            binding.itemPlaceNameTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    placeClickListener(p0.toString(), absoluteAdapterPosition)
                }
            })

            binding.groupLayout.translationX = 0f

            binding.onclickDeleteItem.setOnClickListener {
                deleteItems.add(item.moimActivityId)
                deleteItemList(deleteItems)
                listData.remove(item)
                notifyDataSetChanged()
            }
        }
    }
}
