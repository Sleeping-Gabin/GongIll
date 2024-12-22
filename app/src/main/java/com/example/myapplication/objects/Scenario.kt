package com.example.myapplication.objects

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Scenario(
    val finishedResult: List<GameResult>,
    val teamResults: MutableList<GameResult>
): Parcelable {
    @IgnoredOnParcel
    private val finishedWins = finishedResult.groupingBy { it.winner }.eachCount()
    @IgnoredOnParcel
    var wins = mapOf<Int?, Int>()


    fun update() {
        wins = (finishedResult + teamResults).groupingBy { it.winner }.eachCount()
    }

    fun winNum(idx: Int): Int {
        return wins.getOrDefault(idx,0)
    }

    fun newWinNum(idx: Int): Int {
        return winNum(idx) - finishedWins.getOrDefault(idx,0)
    }

    fun newLoseNum(idx: Int): Int {
        return teamResults.count { it.team1Idx == idx || it.team2Idx == idx } - newWinNum(idx)
    }

    private fun isSameGames(other: Scenario): Boolean {
        if (other.teamResults.size != teamResults.size) return false
        for (i in 0 until teamResults.size) {
            if (!teamResults[i].isSameGame(other.teamResults[i])) return false
        }
        return true
    }

    fun diffResultOne(other: Scenario): Int {
        if (!isSameGames(other)) return -1

        var diff = -1
        for (i in 0 until teamResults.size) {
            if (teamResults[i].winner != other.teamResults[i].winner) {
                if (diff != -1)
                    return -1
                else
                    diff = i
            }
        }
        return diff
    }

    fun copy(): Scenario {
        return Scenario(finishedResult, teamResults.toMutableList())
    }
}
