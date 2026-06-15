package com.example.smartmoneyeverydaysa_1

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

/**
 * A custom view that renders a smooth line graph for financial data.
 * It supports:
 * - Data points with a gradient fill.
 * - Dynamic X-axis labels (timeline).
 * - Minimum and Maximum goal/limit lines.
 */
class LineGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: List<Double> = emptyList()
    private var labels: List<String> = emptyList()
    private var minGoalValue: Double? = null
    private var maxGoalValue: Double? = null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val maxGoalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val minGoalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50") // Green
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.secondary_text_dark)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private val goalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        textAlign = Paint.Align.RIGHT
    }

    /**
     * Updates the graph data and timeline labels.
     */
    fun setData(newData: List<Double>, newLabels: List<String> = emptyList()) {
        data = newData
        labels = newLabels
        invalidate()
    }

    /**
     * Sets the horizontal goal/limit lines to be drawn on the graph.
     */
    fun setGoals(min: Double?, max: Double?) {
        minGoalValue = min
        maxGoalValue = max
        invalidate()
    }

    /**
     * Handles the custom drawing of the graph components.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val padding = 60f
        val bottomPadding = 40f
        val width = width.toFloat() - 2 * padding
        val height = height.toFloat() - 2 * padding - bottomPadding
        
        val maxDataVal = data.maxOrNull() ?: 1.0
        val goalsMax = maxOf(minGoalValue ?: 0.0, maxGoalValue ?: 0.0)
        val effectiveMaxVal = maxOf(maxDataVal, goalsMax).coerceAtLeast(1.0)
        val minVal = 0.0

        // Draw Max Goal Line (Limit)
        maxGoalValue?.let { goal ->
            val y = padding + height - ((goal - minVal) / (effectiveMaxVal - minVal) * height).toFloat()
            canvas.drawLine(padding, y, padding + width, y, maxGoalPaint)
            goalTextPaint.color = Color.RED
            canvas.drawText("LIMIT", padding + width, y - 10f, goalTextPaint)
        }

        // Draw Min Goal Line
        minGoalValue?.let { goal ->
            val y = padding + height - ((goal - minVal) / (effectiveMaxVal - minVal) * height).toFloat()
            canvas.drawLine(padding, y, padding + width, y, minGoalPaint)
            goalTextPaint.color = Color.parseColor("#4CAF50")
            canvas.drawText("MIN GOAL", padding + width, y - 10f, goalTextPaint)
        }

        if (data.size < 2) {
            val x = padding + width / 2
            val y = padding + height - ((data[0] - minVal) / (effectiveMaxVal - minVal) * height).toFloat()
            canvas.drawCircle(x, y, 10f, dotPaint)
            if (labels.isNotEmpty()) {
                canvas.drawText(labels[0], x, padding + height + 35f, textPaint)
            }
            return
        }

        val xStep = width / (data.size - 1)
        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + height - ((value - minVal) / (effectiveMaxVal - minVal) * height).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, padding + height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, padding + height)
                fillPath.close()
            }
        }

        // Draw fill gradient
        val primaryColor = ContextCompat.getColor(context, R.color.primary)
        val primaryTransparent = (primaryColor and 0x00FFFFFF) or 0x44000000

        fillPaint.shader = LinearGradient(
            0f, padding, 0f, padding + height,
            primaryTransparent, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawPath(fillPath, fillPaint)

        // Draw line
        canvas.drawPath(path, linePaint)

        // Draw dots and labels
        data.forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + height - ((value - minVal) / (effectiveMaxVal - minVal) * height).toFloat()
            canvas.drawCircle(x, y, 10f, dotPaint)
            
            // Draw label
            if (labels.isNotEmpty() && index < labels.size) {
                canvas.drawText(labels[index], x, padding + height + 35f, textPaint)
            }
        }
    }
}
