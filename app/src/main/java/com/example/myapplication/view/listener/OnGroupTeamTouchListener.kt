package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Team

interface OnGroupTeamTouchListener {
    fun onTouchItem(team: Team)
}