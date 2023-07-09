package com.example.namo.ui.bottom.group

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.databinding.ActivityGroupInfoBinding

class GroupInfoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGroupInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickListener()
    }

    private fun clickListener() {
        binding.groupInfoCloseBtn.setOnClickListener {
            finish()
        }
    }
}