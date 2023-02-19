package com.example.namo.ui.bottom.grouplist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.databinding.ActivityCreateGroupBinding

class CreateGroupActivity: AppCompatActivity() {

    lateinit var binding: ActivityCreateGroupBinding

    var access: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        onClickListener() //클릭 동작
    }


    fun onClickListener() {

        binding.createGroupBackTv.setOnClickListener {
            finish() //뒤로가기
        }

        //앨범 권한 확인 후 연결
        binding.createGroupCoverImgIv.setOnClickListener {
//            getPermission()
        }

//        binding.createGroupSaveTv.setOnClickListener {
//            Toast.makeText(this, "생성 버튼 클릭", Toast.LENGTH_SHORT).show()
//
//            binding.addGroupSaveTv.setText(R.string.save) //생성 -> 저장으로 텍스트 바꿔주기
//
//            binding.addGroupCodeCopyIv.visibility = View.VISIBLE
//
//            binding.addGroupSaveTv.setOnClickListener { //저장
//                Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
//                finish() //한 번 더 버튼 누르면 종료
//            }
//
//        }

    }
}