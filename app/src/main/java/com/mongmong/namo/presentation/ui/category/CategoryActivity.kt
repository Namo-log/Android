package com.mongmong.namo.presentation.ui.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityCategoryBinding
import com.mongmong.namo.presentation.config.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryActivity : BaseActivity<ActivityCategoryBinding>(R.layout.activity_category) {
    override fun setup() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.category_frm, CategorySettingFragment())
            .commitAllowingStateLoss()

        binding.categoryDarkView.setOnClickListener {
            finish()
        }
    }
}