package com.gabin.gongill.view.listener

import com.gabin.gongill.view.adapter.PlayAdapter

/**
 * [PlayAdapter]에서 아이템 터치시 호출되는 Interface
 */
interface OnPointTouchListener {
	/**
	 * 세트 데이터 아이템 터치시 호출
	 *
	 * @param[set] 터치한 세트 데이터의 인덱스
	 */
	fun onTouchItem(set: Int)
}