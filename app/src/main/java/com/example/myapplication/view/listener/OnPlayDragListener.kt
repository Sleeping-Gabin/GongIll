package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Play

interface OnPlayDragListener {
    fun onDragItem(from: Play, to: Play)
    fun onTouchItem(play: Play)
}