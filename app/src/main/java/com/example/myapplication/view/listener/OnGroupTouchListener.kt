package com.example.myapplication.view.listener

import com.example.myapplication.database.entity.Group

interface OnGroupTouchListener {
    fun onTouchItem(group: Group)
    fun onTouchButton(group: Group)
    fun onTouchHeader(category: String)
}