package com.example.namo.ui.bottom.diary.groupDiary.adapter

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
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.databinding.ItemDiaryGroupEventBinding
import java.text.NumberFormat
import java.util.*


class GroupPlaceEventAdapter(
    // 그룹 다이어리 장소 추가, 정산, 이미지
    val context: Context,
    private val listData: MutableList<DiaryGroupEvent>,
    val payClickListener: (pay: Long, position: Int, payText: TextView) -> Unit,
    val imageClickListener: (imgLists: ArrayList<String?>, position: Int) -> Unit,
    val placeClickListener: (text: String, position: Int) -> Unit,
    val deleteItemList: (deleteItems: MutableList<Long>) -> Unit
) : RecyclerView.Adapter<GroupPlaceEventAdapter.Holder>() {

    private val items = arrayListOf<ArrayList<String?>>()
    private var deleteItems = arrayListOf<Long>()

    @SuppressLint("NotifyDataSetChanged")
    fun addImageItem(image: ArrayList<String?>) {
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

        if (event.imgs.isNotEmpty()) {
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
        adapter.addItem(event.imgs)

        holder.bind(event)

    }

    override fun getItemCount(): Int = listData.size


    inner class Holder(val binding: ItemDiaryGroupEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: DiaryGroupEvent) {

            if (item.place == "장소") {
                binding.itemPlaceNameTv.text = null
                binding.itemPlaceNameTv.hint = "장소"
            } else {
                binding.itemPlaceNameTv.setText(item.place)
            }

            binding.itemPlaceNameTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    placeClickListener(p0.toString(), absoluteAdapterPosition)
                }
            })

            binding.groupLayout.translationX = 0f

            binding.onclickDeleteItem.setOnClickListener {
                deleteItems.add(item.placeIdx)
                deleteItemList(deleteItems)
                listData.remove(item)
                notifyDataSetChanged()
            }
        }
    }
}
