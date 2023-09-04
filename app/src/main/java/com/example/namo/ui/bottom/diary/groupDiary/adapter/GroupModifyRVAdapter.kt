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


class GroupModifyRVAdapter(
    val context: Context,
    private val listData: MutableList<DiaryGroupEvent>,
) : RecyclerView.Adapter<GroupModifyRVAdapter.Holder>(),
    ItemTouchHelperListener {

    private val items = arrayListOf<ArrayList<String?>>()

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: ArrayList<String?>) {
        this.items.add(image)
        notifyDataSetChanged()
    }

    /** 금액 정산 화살표 누르면 정산 다이얼로그로 이동**/
    interface PayInterface {
        fun onPayClicked(pay: Long, position: Int, eventPay: DiaryGroupEvent, payText: TextView)
    }

    private lateinit var groupPayClickListener: PayInterface
    fun groupPayClickListener(itemClickListener: PayInterface) {
        groupPayClickListener = itemClickListener
    }

    /** 장소별 이미지 추가 **/
    interface GalleryInterface {
        fun onGalleryClicked(
            imgLists: ArrayList<String?>,
            position: Int,
            eventImage: DiaryGroupEvent
        )
    }

    private lateinit var groupGalleryClickListener: GalleryInterface
    fun groupGalleryClickListener(itemClickListener: GalleryInterface) {
        groupGalleryClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ItemDiaryGroupEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: Holder, position: Int) {

        val event = listData[position]

        // 장소
        holder.place.setText(event.place)
        holder.place.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                val updatedPosition = holder.bindingAdapterPosition
                if (updatedPosition != RecyclerView.NO_POSITION) {
                    listData[updatedPosition] = event.copy(place = p0.toString())
                }
            }
        })


        // 정산 다이얼로그
        holder.money.text = event.pay.toString()
        holder.payClick.setOnClickListener {
            val updatedPosition = holder.bindingAdapterPosition
            groupPayClickListener.onPayClicked(event.pay, updatedPosition, event, holder.money)
        }

        // 장소별 이미지 가져오기

        val adapter = GroupPlaceGalleryAdapter(context)
        holder.galleryAdapter.adapter = adapter
        holder.galleryAdapter.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        if (event.imgs.isEmpty()) {
            holder.binding.groupGalleryLv.visibility = View.VISIBLE
            holder.binding.groupAddGalleryRv.visibility = View.GONE
        } else {
            holder.binding.groupGalleryLv.visibility = View.GONE
            holder.binding.groupAddGalleryRv.visibility = View.VISIBLE
        }

        holder.gallery.setOnClickListener {
            val updatedPosition = holder.bindingAdapterPosition
            groupGalleryClickListener.onGalleryClicked(event.imgs, updatedPosition, event)
        }

        adapter.addItem(event.imgs)
        adapter.notifyDataSetChanged()

    }

    override fun getItemCount(): Int {
        return listData.size
    }


    inner class Holder(val binding: ItemDiaryGroupEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val place = binding.itemPlaceNameTv
        val payClick = binding.clickMoneyLy
        val money = binding.itemPlaceMoneyTv
        val gallery = binding.groupGalleryLv
        val galleryAdapter = binding.groupAddGalleryRv
    }

    override fun onItemSwipe(position: Int) {
        listData.removeAt(position)
        notifyItemRemoved(position)
    }
}
