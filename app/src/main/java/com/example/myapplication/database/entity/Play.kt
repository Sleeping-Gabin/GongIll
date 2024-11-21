package com.example.myapplication.database.entity

import androidx.room.*
import com.example.myapplication.objects.ChangeData

@Entity(primaryKeys = ["group_name", "team1", "team2", "play_num"],
    foreignKeys = [ForeignKey(
        entity = Team::class,
        parentColumns = ["group_name", "alias"],
        childColumns = ["group_name", "team1"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Team::class,
        parentColumns = ["group_name", "alias"],
        childColumns = ["group_name", "team2"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE)
    ])
data class Play(
    @ColumnInfo(name = "group_name") val group: String,
    val team1: String,
    val team2: String,
) {
    @ColumnInfo(name = "group_order") var order: Int = 1
    @ColumnInfo(name = "play_num") var playNum: Int = 0

    @ColumnInfo(name = "round_result") var roundResult: List<Int?> = listOf(null, null, null, null)
    //0: team1 승 / 1: team2 승 / 2: 무승부 / null: 경기 안함
    @ColumnInfo(name = "point_result") var pointResult: List<List<Int>> = List(4) { listOf(0, 0) }
    @ColumnInfo(name = "win_team") var winTeam: String? = null
    @ColumnInfo(name = "round_count") var roundCount: Int = 2

    val winIdx: Int?
        get() {
            return when (winTeam) {
                team1 -> 0
                team2 -> 1
                else -> null
            }
        }

    constructor(group: String, team1: String, team2: String, playNum: Int):
            this(group, team1, team2) {
                    this.playNum = playNum
            }


    fun clear(): ChangeData {
        val changeData = ChangeData(this)

        roundResult = listOf(null, null, null, null)
        pointResult = List(4) { listOf(0, 0) }
        winTeam = null
        roundCount = 0

        changeData.changedData(this)

        return changeData
    }

    fun changeResult(round: Int, point: List<Int>, timeWin: Int? = null): ChangeData {
        val changeData = ChangeData(this)

        val newRoundResult = roundResult.toMutableList()
        val newPointResult = pointResult.toMutableList()

        if (point[0] > point[1]) {
            newRoundResult[round] = 0
        }
        else if (point[0] < point[1]) {
            newRoundResult[round] = 1
        }
        else {
            if (round == 3)
                newRoundResult[round] = timeWin
            else
                newRoundResult[round] = 2
        }

        newPointResult[round] = point

        roundResult = newRoundResult.toList()
        pointResult = newPointResult.toList()

        if (isNeedExtra())
            roundCount = 4
        else {
            roundCount = 3
            newRoundResult[3] = null
            newPointResult[3] = listOf(0, 0)
        }
        if (round < 2 && !isNeed3()) {
            roundCount = 2
            newRoundResult[2] = null
            newPointResult[2] = listOf(0, 0)
        }

        roundResult = newRoundResult
        pointResult = newPointResult
        updateWinTeam()

        changeData.changedData(this)

        return changeData
    }

    private fun isNeedExtra(): Boolean {
        val round3Result = roundResult.subList(0, 3)
        val point3Result = pointResult.subList(0, 3)
        return if (round3Result[0]==null || round3Result[1]==null || round3Result[2]==null)
            false
        else if (round3Result.count { result -> result==0 } != round3Result.count { result -> result==1 } )
            false
        else point3Result.sumOf { point -> point[0] } == point3Result.sumOf { point -> point[1] }
    }

    private fun isNeed3(): Boolean {
        return if (roundResult[0] == 0 && roundResult[1] == 1)
            true
        else if (roundResult[0] == 1 && roundResult[1] == 0)
            true
        else roundResult[0] == 2 || roundResult[1] == 2
    }

    private fun updateWinTeam() {
        when (roundCount) {
            2 -> {
                winTeam = if (roundResult[0] == 0 && roundResult[1] == 0)
                    team1
                else if (roundResult[0] == 1 && roundResult[1] == 1)
                    team2
                else
                    null
            }
            3 -> {
                val round3Result = roundResult.subList(0, 3)
                val point3Result = pointResult.subList(0, 3)
                winTeam = if (roundResult[2] == null)
                    null
                else if (round3Result.count { result -> result==0 } == round3Result.count { result -> result==1 }) {
                    if (point3Result.sumOf { point -> point[0] } < point3Result.sumOf { point -> point[1] } )
                        team2
                    else
                        team1
                } else if (round3Result.count { result -> result==0 } < round3Result.count { result -> result==1 })
                    team2
                else
                    team1
            }
            4 -> {
                winTeam = when (roundResult[3]) {
                    0 -> team1
                    1 -> team2
                    else -> null
                }
            }
        }
    }
}