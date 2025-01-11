package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Team
import com.example.myapplication.view.adapter.GroupTeamAdapter

/**
 * [GroupTeamAdapter]에서 아이템 터치 시 호출되는 Interface
 */
interface OnGroupTeamTouchListener {
    /**
     * 팀을 터치했을 때 호출
     *
     * @param[team] 터치한 팀
     */
    fun onTouchItem(team: Team)
}