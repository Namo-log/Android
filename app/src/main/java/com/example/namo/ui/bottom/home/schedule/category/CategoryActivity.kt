package com.example.namo.ui.bottom.home.schedule.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.R
import com.example.namo.databinding.ActivityCategoryBinding


class CategoryActivity : AppCompatActivity() {

    lateinit var binding: ActivityCategoryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportFragmentManager.beginTransaction()
            .replace(R.id.category_frm, CategorySettingFragment())
            .commitAllowingStateLoss()

        binding.floatingCategoryDarkView.setOnClickListener {
            finish()
        }
    }

}