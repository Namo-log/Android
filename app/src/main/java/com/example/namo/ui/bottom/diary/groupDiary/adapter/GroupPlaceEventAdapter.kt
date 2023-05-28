package com.example.namo.ui.bottom.diary.groupDiary.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.databinding.ItemDiaryGroupEventBinding


class GroupPlaceEventAdapter(  // 그룹 다이어리 장소 추가, 정산, 이미지
    val context: Context,
    private val listData: List<DiaryGroupEvent>,
    initialItems: List<String> = emptyList()
    ) : RecyclerView.Adapter<GroupPlaceEventAdapter.Holder>(){

    val items= arrayListOf<ArrayList<String>?>()
   // private val items = ArrayList<String>(initialItems)

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(image: ArrayList<String>){
       // this.items.clear()
       this.items.add(image)
        notifyDataSetChanged()

        Log.d("adapterimg",items.toString())
    }

    /** 금액 정산 화살표 누르면 정산 다이얼로그로 이동**/
    interface PayInterface {
        fun onPayClicked(pay:Int)
    }
    private lateinit var groupPayClickListener: PayInterface
    fun groupPayClickListener(itemClickListener: PayInterface) {
        groupPayClickListener= itemClickListener
    }

    /** 장소별 이미지 추가 **/
    interface GalleryInterface {
        fun onGalleryClicked()
    }
    private lateinit var groupGalleryClickListener: GalleryInterface
    fun groupGalleryClickListener(itemClickListener: GalleryInterface) {
        groupGalleryClickListener= itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemDiaryGroupEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val member = listData[position]
        holder.setData(member, position)

        Log.d("size",listData.size.toString())
        Log.d("pos",member.place)
        Log.d("memberpos",position.toString())

        // 정산 다이얼로그
        holder.binding.itemPlaceMoneyIv.setOnClickListener {
            groupPayClickListener.onPayClicked(member.pay)
             holder.binding.itemPlaceMoneyTv.text=member.pay.toString()
        }
       // holder.binding.itemPlaceMoneyTv.text=member.pay.toString()

       // holder.binding.groupGalleryLv.setOnClickListener {
//            holder.binding.groupGalleryLv.visibility=View.GONE
//            holder.binding.groupAddGalleryRv.visibility=View.VISIBLE
//
//            member.imgs?.let { groupGalleryClickListener.onGalleryClicked() }
//
//            holder.gallery.adapter=
//                items[position]?.let { it1 -> GroupPlaceGalleryAdapter(context, it1) }
//            Log.d("adapteritem",items.toString())
//            Log.d("adapterpos",items[position].toString())
     //   }

        holder.binding.itemPlaceNameTv.setText(member.place)

    }
    override fun getItemCount(): Int {
        return listData.size
    }


    inner class Holder(val binding: ItemDiaryGroupEventBinding) : RecyclerView.ViewHolder(binding.root) {

        var mMember: DiaryGroupEvent? = null
        var mPosition: Int? = null

        val gallery=binding.groupAddGalleryRv
        fun setData(item: DiaryGroupEvent, position: Int) {

            binding.itemPlaceNameTv.setText(item.place)

            binding.apply {
//                groupAddGalleryRv.adapter=GroupPlaceGalleryAdapter(context,items)
                groupAddGalleryRv.layoutManager=LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            }

            this.mMember = item
            this.mPosition = position
        }
    }
}