package com.example.namo.bottom.home.calendar

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import com.example.namo.R
import com.example.namo.utils.CalendarUtils.Companion.getDateColor
import com.example.namo.utils.CalendarUtils.Companion.isSameMonth
import org.joda.time.DateTime

class DayItemView @JvmOverloads constructor(
    context: Context,
    attrs : AttributeSet? = null,
    @AttrRes private val defStyleAttr : Int = R.attr.itemViewStyle,
    @StyleRes private val defStyleRes : Int = R.style.Calendar_ItemViewStyle,
    private val date : DateTime = DateTime(),
    private val firstDayOfMonth : DateTime = DateTime()
) : View(ContextThemeWrapper(context, defStyleRes), attrs, defStyleAttr) {

    private val bounds = Rect()
    private var paint : Paint = Paint()

    private var today = DateTime().withTimeAtStartOfDay().millis

    init {
        context.withStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes) {
            val dayTextSize = getDimensionPixelSize(R.styleable.CalendarView_dayTextSize, 0).toFloat()

            paint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = Color.BLACK
                if (!isSameMonth(date, firstDayOfMonth)) {
                    alpha = 50
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        val day = date.dayOfMonth.toString()

        if (date.withTimeAtStartOfDay().isEqual(today)) {
            paint.setColor(resources.getColor(R.color.MainOrange))
        }

        paint.getTextBounds(day, 0, day.length, bounds)
        canvas.drawText(
            day,
            0.toFloat(),
            (bounds.height()).toFloat(),
            paint
        )

        setOnClickListener {
            Log.d("DAYITEMVIEW_CLICK", "${date.year}년 ${date.monthOfYear}월 ${date.dayOfMonth}일 ${date.dayOfWeek}요일")
        }
    }
}