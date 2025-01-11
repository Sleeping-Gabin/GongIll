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

/**
 * 경기 데이터 클래스
 *
 * @param[name] 그룹 이름
 * @param[playNum] 같은 팀과의 경기 횟수
 *
 * @property[category] 그룹의 카테고리. 미지정 시 "others"
 *
 * @constructor 새로운 그룹을 생성. 카테고리 미지정 시 "others"로 설정
 */
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