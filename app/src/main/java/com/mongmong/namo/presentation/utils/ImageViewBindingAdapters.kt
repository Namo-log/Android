package com.mongmong.namo.presentation.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData

object ImageViewBindingAdapters {
    @JvmStatic
    @BindingAdapter("android:imageSource")
    fun setImageSource(imageView: ImageView, resource: LiveData<Int?>?) {
        if (resource != null && resource.value != null) {
            imageView.setImageResource(resource.value!!)
        }
    }
}
