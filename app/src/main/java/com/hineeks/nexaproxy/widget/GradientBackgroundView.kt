/******************************************************************************
 *                                                                            *
 * Copyright (C) 2026 by hineeks                                              *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package com.hineeks.nexaproxy.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.hineeks.nexaproxy.R
import com.hineeks.nexaproxy.database.DataStore
import com.hineeks.nexaproxy.utils.Theme
import kotlin.math.sin

class GradientBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var startColor: Int = context.getColor(R.color.material_indigo_700)
    private var endColor: Int = context.getColor(R.color.material_deep_purple_700)

    private var phase = 0f
    private var animator: ValueAnimator? = null

    private fun updateThemeColors() {
        startColor = context.getColor(
            if (DataStore.appTheme == Theme.BLACK) R.color.material_deep_purple_900 else R.color.material_indigo_700
        )
        endColor = context.getColor(
            if (DataStore.appTheme == Theme.BLACK) R.color.material_indigo_900 else R.color.material_deep_purple_700
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return
        updateThemeColors()
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 24000
                interpolator = DecelerateInterpolator()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                addUpdateListener {
                    phase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            animator?.start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val drift = (sin(phase * Math.PI * 2) * w * 0.12).toFloat()
        val shader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LinearGradient(
                0f + drift, 0f,
                w - drift, h,
                intArrayOf(startColor, endColor),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f, 0f, w, h, intArrayOf(startColor, endColor), null, Shader.TileMode.CLAMP
            )
        }
        paint.shader = shader
        canvas.drawRect(0f, 0f, w, h, paint)
    }
}