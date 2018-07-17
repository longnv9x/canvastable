package com.example.long_pc.myapplication

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
* Created by DoanNH on 11-Sep-17.
*/
class NonDraggingBottomSheet<V : View> : BottomSheetBehavior<V> {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor() : super()

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, ev: MotionEvent): Boolean {
        return false
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {

    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray) {

    }

    override fun onNestedScrollAccepted(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, nestedScrollAxes: Int) {

    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return false
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return true
    }
}