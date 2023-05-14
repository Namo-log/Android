package com.example.namo.ui.bottom.home.schedule.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.R
import com.example.namo.databinding.ActivityCategoryEditBinding

class CategoryEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityCategoryEditBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.category_edit_frm, CategoryDetailFragment(true))
            .commitAllowingStateLoss()

        binding.floatingCategoryDarkView.setOnClickListener {
            finish()
        }

    }

}