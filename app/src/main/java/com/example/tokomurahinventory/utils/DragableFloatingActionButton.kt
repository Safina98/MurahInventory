package com.example.tokomurahinventory.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.OvershootInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton

//Source : https://stackoverflow.com/a/56377297/16776791
class DraggableFloatingActionButton : FloatingActionButton, OnTouchListener {
    private var customClickListener: CustomClickListener? = null
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f
    var viewWidth = 0
    var viewHeight = 0
    var parentWidth = 0
    var parentHeight = 0
    var newX = 0f
    var newY = 0f

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val layoutParams = view.layoutParams as MarginLayoutParams
        val action = motionEvent.action
        return if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.x - downRawX
            dY = view.y - downRawY
            false // not Consumed for ripple effect
        } else if (action == MotionEvent.ACTION_MOVE) {
            viewWidth = view.width
            viewHeight = view.height
            val viewParent = view.parent as View
            parentWidth = viewParent.width
            parentHeight = viewParent.height
            newX = motionEvent.rawX + dX
            newX = Math.max(
                layoutParams.leftMargin.toFloat(),
                newX
            ) // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(
                parentWidth - viewWidth - layoutParams.rightMargin.toFloat(),
                newX
            ) // Don't allow the FAB past the right hand side of the parent
            newY = motionEvent.rawY + dY
            newY = Math.max(
                layoutParams.topMargin.toFloat(),
                newY
            ) // Don't allow the FAB past the top of the parent
            newY = Math.min(
                parentHeight - viewHeight - layoutParams.bottomMargin.toFloat(),
                newY
            ) // Don't allow the FAB past the bottom of the parent
            view.animate()
                .x(newX)
                .y(newY)
                .setDuration(0)
                .start()
            true // Consumed
        } else if (action == MotionEvent.ACTION_UP) {
            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY
            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY
            newX = if (newX > (parentWidth - viewWidth - layoutParams.rightMargin) / 2) {
                parentWidth - viewWidth - layoutParams.rightMargin.toFloat()
            } else {
                layoutParams.leftMargin.toFloat()
            }
            view.animate()
                .x(newX)
                .y(newY)
                .setInterpolator(OvershootInterpolator())
                .setDuration(300)
                .start()
            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(
                    upDY
                ) < CLICK_DRAG_TOLERANCE
            ) { // A click
                if (customClickListener != null) {
                    customClickListener!!.onClick(view)
                }
                false // not Consumed for ripple effect
            } else { // A drag
                false // not Consumed for ripple effect
            }
        } else {
            super.onTouchEvent(motionEvent)
        }
    }

    fun setCustomClickListener(customClickListener: CustomClickListener?) {
        this.customClickListener = customClickListener
    }

    interface CustomClickListener {
        fun onClick(view: View?)
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE =
            10f // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    }
}