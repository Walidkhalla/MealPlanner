package com.walid.abahri.mealplanner.util

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape

/**
 * TextDrawable class for creating text-based drawables (like avatars with initials)
 * This is a simplified version inspired by the TextDrawable library
 */
class TextDrawable private constructor(builder: Builder) : ShapeDrawable(builder.shape) {

    private val textPaint: Paint
    private val text: String
    private val color: Int
    private val height: Int
    private val width: Int
    private val fontSize: Int
    private val radius: Float

    init {
        // Set up the shape properties
        height = builder.height
        width = builder.width
        radius = builder.radius
        color = builder.color
        fontSize = builder.fontSize
        text = if (builder.toUpperCase) builder.text.uppercase() else builder.text

        // Set up the paint objects
        val paint = paint
        paint.color = color

        textPaint = Paint().apply {
            color = builder.textColor
            isAntiAlias = true
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            typeface = builder.font
            textSize = builder.fontSize.toFloat()
        }

        // Set the shape bound
        val bounds = Rect(0, 0, width, height)
        shape.resize(width.toFloat(), height.toFloat())
        setBounds(bounds)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val r = bounds

        // Draw text in the center
        val count = canvas.save()
        canvas.translate(r.left.toFloat(), r.top.toFloat())
        val width = this.width
        val height = this.height
        val fontSize = this.fontSize

        // Draw text
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textOffset = (textHeight / 2) - textPaint.descent()
        val textX = width.toFloat() / 2
        val textY = height.toFloat() / 2 + textOffset

        canvas.drawText(text, textX, textY, textPaint)
        canvas.restoreToCount(count)
    }

    /**
     * Builder class for TextDrawable
     */
    class Builder internal constructor() {
        internal var text: String = ""
        internal var color: Int = Color.GRAY
        internal var textColor: Int = Color.WHITE
        internal var borderColor: Int = Color.TRANSPARENT
        internal var borderWidth: Int = 0
        internal var width: Int = -1
        internal var height: Int = -1
        internal var fontSize: Int = -1
        internal var radius: Float = 0f
        internal var shape: android.graphics.drawable.shapes.Shape = RoundRectShape(
            floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            null, null
        )
        internal var font: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        internal var toUpperCase: Boolean = true

        /**
         * Set the text for the drawable
         */
        fun text(text: String): Builder {
            this.text = text
            return this
        }

        /**
         * Set the background color
         */
        fun color(color: Int): Builder {
            this.color = color
            return this
        }

        /**
         * Set the text color
         */
        fun textColor(color: Int): Builder {
            this.textColor = color
            return this
        }

        /**
         * Start configuration
         */
        fun beginConfig(): Builder {
            return this
        }

        /**
         * Set the width and height
         */
        fun width(width: Int): Builder {
            this.width = width
            return this
        }

        /**
         * Set the height
         */
        fun height(height: Int): Builder {
            this.height = height
            return this
        }

        /**
         * Set the font size
         */
        fun fontSize(fontSize: Int): Builder {
            this.fontSize = fontSize
            return this
        }

        /**
         * Set the font typeface
         */
        fun font(font: Typeface): Builder {
            this.font = font
            return this
        }

        /**
         * Set whether text should be uppercase
         */
        fun toUpperCase(toUpperCase: Boolean): Builder {
            this.toUpperCase = toUpperCase
            return this
        }

        /**
         * End configuration
         */
        fun endConfig(): Builder {
            return this
        }

        /**
         * Build a round drawable with text
         */
        fun buildRound(text: String, color: Int): TextDrawable {
            this.text = text
            this.color = color

            if (width == -1) {
                width = 100
            }
            if (height == -1) {
                height = 100
            }
            if (fontSize == -1) {
                fontSize = 50
            }

            // Create round shape
            shape = OvalShape()
            this.radius = 0f

            return TextDrawable(this)
        }
    }

    companion object {
        /**
         * Get a builder for creating TextDrawable objects
         */
        fun builder(): Builder {
            return Builder()
        }
    }
}


