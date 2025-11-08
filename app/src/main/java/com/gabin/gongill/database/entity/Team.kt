package com.gabin.gongill.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.gabin.gongill.objects.ChangeData

@Entity(
	foreignKeys = [ForeignKey(
		entity = Group::class,
		parentColumns = arrayOf("name"),
		childColumns = arrayOf("group_name"),
		onDelete = ForeignKey.CASCADE,
		onUpdate = ForeignKey.CASCADE
	)],
	indices = [Index("group_name")],
	primaryKeys = ["alias", "group_name"]
)
/**
 * 팀 데이터 클래스
 *
 * @param[name] 팀 이름
 * @param[groupName] 팀이 속한 그룹 이름
 *
 * @property[alias] 팀 별칭
 * @property[rank] 팀의 순위
 * @property[win] 승리 횟수
 * @property[lose] 패배 횟수
 * @property[roundWin] 라운드 승점. 승리한 라운드 수 - 패배한 라운드 수
 * @property[roundCount] 라운드 수
 * @property[point] 포인트 합
 * @property[drawRound] 무승부 점수. 패배한 경기의 무승부 수 - 승리한 경기의 무승부 수
 *
 * @constructor 새로운 팀 생성
 */
data class Team(
	val name: String,
	@ColumnInfo("group_name") val groupName: String
) {
	var alias = name
	
	var rank: Int = 0
	var win: Int = 0
	var lose: Int = 0
	@ColumnInfo(name = "round_win")
	var roundWin: Int = 0
	@ColumnInfo(name = "round_count")
	var roundCount = 0
	var point: Int = 0
	@ColumnInfo(name = "draw_round")
	var drawRound = 0
	
	constructor(name: String, groupName: String, alias: String) :
					this(name, groupName) {
		this.alias = alias
	}
	
	/**
	 * 팀 데이터를 초기화한다.
	 */
	fun clear() {
		rank = 0
		win = 0
		lose = 0
		roundWin = 0
		roundCount = 0
		point = 0
		drawRound = 0
	}
	
	/**
	 * 팀의 정보를 문자열로 반환
	 *
	 * @return 팀의 별칭, 승리 횟수, 라운드 승점, 포인트 합계 정보의 문자열
	 */
	override fun toString(): String {
		return "${this.alias}: win ${this.win} / round win ${this.roundWin} / point ${this.point}"
	}
}