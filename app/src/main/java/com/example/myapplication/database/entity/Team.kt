package com.example.myapplication.database.entity

import androidx.room.*

@Entity(foreignKeys = [ForeignKey(
    entity = Group::class,
    parentColumns = arrayOf("name"),
    childColumns = arrayOf("group_name"),
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE
    )],
    indices = [Index("group_name")],
    primaryKeys = ["alias", "group_name"]
)
data class Team(
    val name: String,
    @ColumnInfo("group_name") val groupName: String
) {
    var alias = name

    var rank: Int = 0
    var win: Int = 0
    var lose: Int = 0
    @ColumnInfo(name="round_win") var roundWin: Int = 0
    @ColumnInfo(name="round_count") var roundCount = 0
    var point: Int = 0
    @ColumnInfo(name="draw_round") var drawRound = 0

    constructor(name: String, groupName: String, alias: String):
            this(name, groupName) {
                this.alias = alias
            }

    override fun toString(): String {
        return "${this.alias}: win ${this.win} / round win ${this.roundWin} / round count ${this.roundCount}"
    }
}