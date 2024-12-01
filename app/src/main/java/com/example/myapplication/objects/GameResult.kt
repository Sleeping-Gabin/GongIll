package com.example.myapplication.objects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameResult(
    val team1Idx: Int,
    val team2Idx: Int,
    val playNum: Int,
    var winner: Int?): Parcelable {

    fun isSameGame(other: GameResult): Boolean {
        return team1Idx == other.team1Idx && team2Idx == other.team2Idx && playNum == other.playNum
    }
}