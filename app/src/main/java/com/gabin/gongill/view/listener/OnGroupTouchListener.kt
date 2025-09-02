package com.gabin.gongill.view.listener

import com.gabin.gongill.database.entity.Group
import com.gabin.gongill.view.adapter.GroupAdapter

/**
 * [GroupAdapter]에서 아이템을 터치했을 때 호출되는 Interface
 */
interface OnGroupTouchListener {
	/**
	 * 그룹을 터치했을 때 호출
	 *
	 * @param[group] 터치한 그룹
	 */
	fun onTouchItem(group: Group)
	
	/**
	 * 팀 추가 버튼을 터치했을 때 호출
	 *
	 * @param[group] 팀을 추가할 그룹
	 */
	fun onTouchButton(group: Group)
	
	/**
	 * 그룹 추가 버튼을 터치했을 때 호출
	 *
	 * @param[category] 그룹을 추가할 카테고리
	 */
	fun onTouchHeader(category: String)
}