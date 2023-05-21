package com.example.namo.ui.bottom.diary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.databinding.ItemDiaryGroupEventBinding
import com.example.namo.ui.bottom.diary.groupDiary.GroupDiaryFragment

class GroupPlaceEventAdapter(  // 그룹 다이어리 장소 추가, 정산, 이미지
    val context: Context,
    val listData: MutableList<DiaryGroupEvent> =mutableListOf<DiaryGroupEvent>())
    : RecyclerView.Adapter<Holder>(){

    interface PayInterface {
        fun onPayClicked()
    }
    private lateinit var groupPayClickListener: GroupPlaceEventAdapter.PayInterface
    fun groupPayClickListener(itemClickListener: GroupPlaceEventAdapter.PayInterface) {
        groupPayClickListener= itemClickListener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemDiaryGroupEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val member = listData[position]
        holder.setData(member, position)

        // 정산 다이얼로그
        holder.binding.itemPlaceMoneyIv.setOnClickListener {
            groupPayClickListener.onPayClicked()
        }

        holder.binding.itemPlaceNameTv.setText(member.place)

    }
    override fun getItemCount(): Int {
        return listData.size
    }
}

class Holder(val binding: ItemDiaryGroupEventBinding) : RecyclerView.ViewHolder(binding.root){

    private val gdf = GroupDiaryFragment.getInstance()
    var mMember: DiaryGroupEvent? = null
    var mPosition: Int? = null

    init {

//        binding.btnDelete.setOnClickListener {
//            mainActivity?.deleteMember(mMember!!)
//        }
//
//        binding.btnEdit.setOnClickListener {
//            mainActivity?.editMember(mPosition!!, mMember!!)
//        }
        binding.itemPlaceMoneyIv
    }

    fun setData(item: DiaryGroupEvent, position: Int){

//        var pay:Int=0
//        pay= Activity.arguments?.getInt("scheduleIdx")!!

        binding.itemPlaceNameTv.setText(item.place)

        
        this.mMember = item
        this.mPosition = position
    }
}