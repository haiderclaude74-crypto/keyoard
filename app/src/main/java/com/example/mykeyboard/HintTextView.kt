package com.example.mykeyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.TextView

/** زر نصّه بالوسط ومعه رمز صغير بالزاوية العليا اليمنى (مثل كيبورد سامسونج) */
class HintTextView(
    ctx: Context,
    private val hint: String,
    hintColor: Int,
    private val hintSizePx: Float,
    private val pad: Float
) : TextView(ctx) {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = hintColor
        textSize = hintSizePx
        textAlign = Paint.Align.RIGHT
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(hint, width - pad, pad + hintSizePx * 0.85f, p)
    }
}
