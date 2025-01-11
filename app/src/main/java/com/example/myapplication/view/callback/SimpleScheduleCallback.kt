package com.example.myapplication.view.callback

import android.content.Context
import android.graphics.Canvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.view.adapter.ScheduleAdapter
import java.lang.Float.min

/**
 * [ScheduleAdapter]의 아이템을 스와이프나 드래그할 때 호출되는 ItemTouchHelper의 Callback 클래스
 *
 * @param[adapter] 스와이프나 드래그를 수행할 [ScheduleAdapter]
 * @param[context] Context 객체
 *
 * @property[maxSwipe] 스와이프 시 고정되는 길이.
 * 해당 길이 이상으로 스와이프를 할 경우 수정 버튼이 드러난다.
 * @property[currentX] [currentSwipeHolder]의 X축 위치
 * @property[currentY] [currentSwipeHolder]의 Y축 위치
 * @property[currentSwipeHolder] 스와이프 중인 ViewHolder
 */
class SimpleScheduleCallback(private val adapter: ScheduleAdapter, context: Context):
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    private val maxSwipe = with(Density(context)) {55.dp.toPx()}
    private var currentX = 0f
    private var currentY = 0f
    private var currentSwipeHolder: ScheduleAdapter.ViewHolder? = null

    /**
     * 아이템을 드래그할 때 호출
     *
     * 두 아이템의 위치를 바꾼다.
     *
     * @param[recyclerView] ItemTouchHelper와 연결된 RecyclerView
     * @param[viewHolder] 드래그하는 ViewHolder
     * @param[target] 드래그 한 위치의 ViewHolder
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.dragItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    /**
     * 아이템을 스와이프할 때 호출
     *
     * 아무것도 하지 않는다.
     *
     * @param[viewHolder] 스와이프하는 viewHolder
     * @param[direction] 스와이프하는 방향
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    /**
     * 스와이프 되었다고 판단할 최소 속도를 반환
     *
     * 스와이프가 되지 않도록 한다.
     *
     * @param[defaultValue] 기본 최소 속도
     *
     * @return 스와이프 되었다고 판단할 최소 속도.
     * 스와이프가 되지 않도록 큰 값으로 설정
     */
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        //이 속도를 넘으면 무조건 swipe out
        return defaultValue*1000
    }

    /**
     * 스와이프 되었다고 판단할 최소 스와이프 된 비율(RecyclerView의 경계 대비 스와이프한 길이)을 반환
     *
     * 스와이프하고 손을 뗄때 호출
     *
     * [currentX]에 따라 스와이프 제한 범위 이상으로 스와이프하면
     * [ScheduleAdapter.ViewHolder.isSwiped]를 변경한다.
     *
     * 스와이프가 되지 않도록 한다.
     *
     * @param[viewHolder] 스와이프 하는 ViewHolder
     *
     * @return 스와이프 되었다고 판단할 최소 스와이프 된 비율.
     * 스와이프가 되지 않도록 2f를 설정
     */
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        //이 이상 swipe하면 무조건 swipe out
        //swipe하고 손을 뗄때 호출됨
        val holder = viewHolder as ScheduleAdapter.ViewHolder

        //스와이프 제한 범위 이상으로 스와이프하면 스와이프 상태를 변경
        //스와이프 제한 범위 이하로 스와이프하면 스와이프 취소
        holder.isSwiped = if(currentX < -maxSwipe)
            !holder.isSwiped
        else
            false

        //스와이프 되지 않게 함
        return 2f
    }

    /**
     * 사용자의 상호작용으로 child를 다시 그릴 때 호출
     *
     * viewHolder를 왼쪽으로 스와이프 할 때, 이전에 스와이프 고정된 아이템을 원래대로 되돌리고,
     * 스와이프 한 아이템의 점수 프레임이 [maxSwipe] 위치 이상으로 되돌아가지 않게 해 수정 버튼이 드러나게 한다.
     *
     * @param[c] child를 그릴 Canvas 객체
     * @param[recyclerView] ItemTouchHelper에 연결된 RecyclerView
     * @param[viewHolder] 상호작용한 viewHolder
     * @param[dX] X축 방향으로 이동시킨 거리
     * @param[dY] Y축 방향으로 이동시킨 거리
     * @param[actionState] 상호작용의 종류.
     * [ItemTouchHelper.ACTION_STATE_SWIPE] 또는 [ItemTouchHelper.ACTION_STATE_DRAG]
     * @param[isCurrentlyActive] 현재 상호작용 중인지 여부
     */
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
                if (holder!=currentSwipeHolder) { //이전과 다른 view holder를 스와이프
                    if (currentSwipeHolder != null) { //이전 스와이프된 view holder를 원래 위치로
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
                //스와이프 된 상태인 경우(maxSwipe 이상으로 스와이프 후 손을 놓은 상태 포함)
                if (holder.isSwiped) {
                    //스와이프 중이면 x축의 위치는 움직인 거리(dx) + 스와이프 고정 길이(maxSwipe)
                    //스와이프 후 손을 놓은 상태이면 maxSwipe를 넘어서 되돌아가지 않음
                    x = if (isCurrentlyActive)
                        -maxSwipe + dX
                    else
                        min(dX, -maxSwipe)
                }
                x = min(x, 0f) //오른쪽으로 스와이프 되지 않음

                currentX = x
                currentY = dY
                //수정 버튼 위의 점수 아이템만 움직임
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