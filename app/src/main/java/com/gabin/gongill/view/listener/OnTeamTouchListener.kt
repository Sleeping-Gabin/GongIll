package com.gabin.gongill.view.listener

import com.gabin.gongill.database.entity.Team
import com.gabin.gongill.view.adapter.RankAdapter

/**
 * [RankAdapter]에서 아이템 터치 시 호출되는 Interface
 */
interface OnTeamTouchListener {
	/**
	 * 팀 터치 시 호출
	 *
	 * @param[team] 터치한 팀
	 */
	fun onTouchItem(team: Team)
}