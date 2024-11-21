package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Team

interface OnTeamTouchListener {
    fun onTouchItem(team: Team)
}