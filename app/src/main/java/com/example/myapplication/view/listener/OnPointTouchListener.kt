package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Play

/**
 * [PlayAdapter]에서 아이템 터치시 호출되는 Interface
 */
interface OnPointTouchListener {
    /**
     * 세트 데이터 아이템 터치시 호출
     *
     * @param[play] 현재 경기의 [Play] 객체
     * @param[set] 터치한 세트 데이터의 인덱스
     */
    fun onTouchItem(play: Play, set: Int)
}