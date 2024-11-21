package com.example.myapplication.view.callback

import android.content.Context
import android.graphics.Canvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.view.adapter.ScheduleAdapter
import java.lang.Float.max
import java.lang.Float.min

class SimpleScheduleCallback(private val adapter: ScheduleAdapter, private val context: Context):
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    private val maxSwipe = with(Density(context)) {55.dp.toPx()}
    var swipeLength = 0f
    var currentX = 0f
    var currentY = 0f
    var currentSwipeHolder: ScheduleAdapter.ViewHolder? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.dragItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        //이 속도를 넘으면 무조건 swipe out
        return defaultValue*1000
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        //이 이상 swipe하면 무조건 swipe out
        //swipe하고 손을 뗄때 호출됨
        val holder = viewHolder as ScheduleAdapter.ViewHolder

        swipeLength = if (holder.isSwiped)
            currentX + maxSwipe
        else
            currentX

        holder.isSwiped = if(currentX < -maxSwipe)
            !holder.isSwiped
        else
            false

        return 2f
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                val holder = viewHolder as ScheduleAdapter.ViewHolder
                if (holder!=currentSwipeHolder) {
                    if (currentSwipeHolder != null) {
                        getDefaultUIUtil().onDraw(
                            c,
                            recyclerView,
                            currentSwipeHolder!!.binding.scheduleFrame,
                            0f,
                            currentY,
                            actionState,
                            isCurrentlyActive
                        )
                        currentSwipeHolder!!.isSwiped = false
                    }
                    currentSwipeHolder = holder
                }

                var x =  dX
                if (holder.isSwiped) {
                    x = if (isCurrentlyActive) {
                        if (dX < 0) dX - maxSwipe
                        else -maxSwipe + dX
                    }
                    else
                        min(dX, -maxSwipe)
                }
                x = min(x, 0f)

                currentX = x
                currentY = dY
                getDefaultUIUtil().onDraw(c, recyclerView, holder.binding.scheduleFrame, x, dY, actionState, isCurrentlyActive)
            }
            else -> super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    /*
    private fun dp2px(dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }
     */
}