package com.example.namo.bottom.home.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import com.example.namo.R

class DayView @JvmOverloads constructor(
    context: Context,
    attrs : AttributeSet? = null,
    @AttrRes private val defStyleAttr : Int = R.attr.itemViewStyle,
    @StyleRes private val defStyleRes : Int = R.style.Calendar_ItemViewStyle,
    private val day : Int = 0,
) : View(ContextThemeWrapper(context, defStyleRes), attrs, defStyleAttr) {

    private val bounds = Rect()
    private var paint : Paint = Paint()

    init {
        context.withStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes) {
            val dayTextSize = getDimensionPixelSize(R.styleable.CalendarView_dayTextSize, 0).toFloat()

            paint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = Color.BLACK
                alpha = 50
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        val dayString : String = when(day) {
            0 -> "일"
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            else -> "날"
        }
        paint.getTextBounds(dayString, 0, dayString.length, bounds)
        canvas.drawText(
            dayString,
            (width / 2 - bounds.width() / 2).toFloat() - 2,
            (height / 2 + bounds.height() / 2).toFloat(),
            paint
        )
    }
}