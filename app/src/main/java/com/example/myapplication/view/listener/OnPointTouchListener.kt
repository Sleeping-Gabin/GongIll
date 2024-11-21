package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Play

interface OnPointTouchListener {
    fun onTouchItem(play: Play, set: Int)
}