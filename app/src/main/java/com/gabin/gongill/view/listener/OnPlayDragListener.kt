package com.gabin.gongill.view.listener

import com.gabin.gongill.database.entity.Play
import com.gabin.gongill.view.adapter.ScheduleAdapter

/**
 * [ScheduleAdapter]에서 사용하는 Interface
 */
interface OnPlayDragListener {
	/**
	 * 드래그 이벤트가 발생했을 때 호출
	 *
	 * @param[from] 드래그한 경기 데이터의 [Play] 객체
	 * @param[to] 드래그한 위치의 경기 데이터의 [Play] 객체
	 */
	fun onDragItem(from: Play, to: Play)
	
	/**
	 * 경기 데이터 아이템 터치 시 호출
	 *
	 * @param[play] 터치한 경기의 [Play] 객체
	 */
	fun onTouchItem(play: Play)
}