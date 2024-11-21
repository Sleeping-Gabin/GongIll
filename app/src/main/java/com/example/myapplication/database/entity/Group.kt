package com.example.myapplication.database.entity

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity (tableName = "groups")
data class Group (
    @PrimaryKey val name: String,
    var playNum:Int = 1
) {
    var category: String = "others"

    constructor(category: String, name: String, playNum: Int):
            this(name, playNum) {
                this.category = category
            }
}