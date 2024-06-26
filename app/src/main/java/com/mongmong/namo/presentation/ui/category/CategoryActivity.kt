package com.mongmong.namo.presentation.ui.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {

    lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.category_frm, CategorySettingFragment())
            .commitAllowingStateLoss()

        binding.categoryDarkView.setOnClickListener {
            finish()
        }
    }

}