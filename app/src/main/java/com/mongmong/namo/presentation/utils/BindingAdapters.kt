package com.mongmong.namo.presentation.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.mongmong.namo.R
import com.gradu.domain.model.ActivityParticipant
import com.mongmong.namo.presentation.enums.CategoryColor
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("android:imageSource")
    fun setImageSource(imageView: ImageView, resource: LiveData<Int?>?) {
        if (resource != null && resource.value != null) {
            imageView.setImageResource(resource.value!!)
        }
    }

    @JvmStatic
    @BindingAdapter("app:tintColor")
    fun setTintColor(view: View, color: Int) {
        val hexColor = when (color) {
            in 1..CategoryColor.getAllHexColors().size -> CategoryColor.getAllHexColors()[color - 1]
            else -> CategoryColor.NAMO_PINK.hexColor
        }
        view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hexColor))
    }

    @JvmStatic
    @BindingAdapter("drawableTintColor")
    fun setDrawableTintColor(view: AppCompatTextView, color: Int) {
        // TextView의 drawableEnd를 가져옴
        val drawables = view.compoundDrawablesRelative
        val drawableEnd = drawables[2] // drawableEnd는 배열의 세 번째 요소

        drawableEnd?.let {
            // 전달된 color 값을 사용하여 tint 적용
            it.setTint(color)
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawables[0], drawables[1], it, drawables[3]
            )
        }
    }

    @JvmStatic
    @BindingAdapter("app:isGoneIfEmpty")
    fun setGoneIfEmpty(view: View, images: List<Any>?) {
        view.visibility = if (images.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("app:imageTint")
    fun setImageViewTint(imageView: ImageView, color: Int) {
        imageView.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(imageView.context, color))
    }

    @JvmStatic
    @BindingAdapter("app:imageUrl", "app:placeHolder")
    fun setImage(imageView : ImageView, url : String?, placeHolder: Drawable?) {
        if (url == null) {
           imageView.setImageDrawable(placeHolder)
        }
        if (placeHolder == null) {
            Glide.with(imageView.context)
                .load(url)
                .into(imageView)
        }
        Glide.with(imageView.context)
            .load(url)
            .placeholder(placeHolder)
            .into(imageView)
    }

    @JvmStatic
    @BindingAdapter("app:tint")
    fun ImageView.setImageTint(@ColorInt color: Int) {
        setColorFilter(color)
    }

    @JvmStatic
    @BindingAdapter(value = ["participantsText", "maxCount"], requireAll = false)
    fun setParticipantsText(textView: TextView, participants: List<com.gradu.domain.model.ActivityParticipant>?, maxCount: Int?) {
        val maxCount = maxCount ?: 3

        participants?.let {
            if (it.isEmpty()) {
                // 참가자가 없을 때
                textView.text = textView.context.getString(R.string.moim_diary_none)
            } else {
                // 참가자가 있을 때 처리
                val size = participants.size
                val displayedNames = participants.take(maxCount).joinToString(", ") { participant -> participant.nickname }

                textView.text = if (size > maxCount) {
                    "$displayedNames 외 ${size - maxCount}명"
                } else {
                    displayedNames
                }
            }
        } ?: run {
            textView.text = textView.context.getString(R.string.diary_no_place)
        }
    }

    @JvmStatic
    @BindingAdapter("currencyText")
    fun setCurrencyEditText(editText: EditText, amount: BigDecimal?) {
        if (amount == null) return

        val formattedText = if (amount == BigDecimal.ZERO)  ""
        else NumberFormat.getNumberInstance(Locale.US).format(amount.toInt()) + " 원"

        if (editText.text.toString() != formattedText) {
            editText.setText(formattedText)
        }
    }

    @JvmStatic
    @BindingAdapter("currencyText")
    fun setCurrencyText(textView: TextView, amount: BigDecimal?) {
        if (amount == null) return
        val formattedText = NumberFormat.getNumberInstance(Locale.US).format(amount.toInt()) + " 원"
        textView.text = formattedText
    }

    @JvmStatic
    @BindingAdapter("totalCurrencyText")
    fun setTotalCurrencyText(textView: TextView, amount: BigDecimal?) {
        if (amount == null) return
        val formattedText = "총 " + NumberFormat.getNumberInstance(Locale.US).format(amount.toInt()) + " 원"
        textView.text = formattedText
    }

    @JvmStatic
    @BindingAdapter("totalAmount", "activityCnt", requireAll = false)
    fun setTotalAmount(textView: TextView, amount: BigDecimal?, activityCnt: Int) {
        val formattedText = if (activityCnt == 0) {
            textView.context.getString(R.string.moim_diary_none_activity)
        } else {
            "총 " + NumberFormat.getNumberInstance(Locale.US).format(amount?.toInt() ?: 0) + " 원"
        }
        textView.text = formattedText
    }

    @JvmStatic
    @BindingAdapter("activityTotalAmount")
    fun setActivityTotalAmount(textView: TextView, amount: BigDecimal?) {
        val formattedText = if (amount == null || amount == BigDecimal.ZERO) textView.context.getString(R.string.moim_diary_none_activity)
        else "총 " + NumberFormat.getNumberInstance(Locale.US).format(amount.toInt()) + " 원"
        textView.text = formattedText
    }

    @JvmStatic
    @BindingAdapter("drawableTintColor")
    fun setDrawableTintColor(textView: TextView, color: Int) {
        textView.compoundDrawableTintList = ColorStateList.valueOf(color)
    }

    @JvmStatic
    @BindingAdapter("app:registerImage")
    fun setRegisterImage(view: ImageView, profileImage: Uri?) {
        // 프로필 이미지가 없을 때 패딩 처리
        val paddingDp = if (profileImage != null) 0 else 46 // dp 단위
        val density = view.context.resources.displayMetrics.density
        val paddingPx = (paddingDp * density).toInt() // dp -> px 변환
        view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

        profileImage?.let {
            // dp 단위를 픽셀 단위로 변환
            val radiusDp = 24f
            val radiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                radiusDp,
                view.context.resources.displayMetrics
            ).toInt()

            Glide.with(view.context)
                .load(it)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("app:centerCropImage")
    fun setCenterCropImage(view: ImageView, profileImage: String?) {
        profileImage?.let {
            val radiusDp = 24f
            val radiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                radiusDp,
                view.context.resources.displayMetrics
            ).toInt()

            Glide.with(view.context)
                .load(it)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(view)
        }
    }

}
