package com.gabin.gongill.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.gabin.gongill.objects.ChangeData

@Entity(
	primaryKeys = ["group_name", "team1", "team2", "play_num"],
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
		onUpdate = ForeignKey.CASCADE
	)
	]
)
/**
 * 경기 데이터 클래스
 *
 * @param[group] 경기가 속한 그룹 이름
 * @param[team1] 첫번째 팀 별칭
 * @param[team2] 두번째 팀 별칭
 *
 * @property[order] 그룹 내 경기 일정의 순서
 * @property[playNum] 같은 팀간의 경기 순서
 * @property[roundResult] 연장전까지의 라운드 결과.
 * 0이면 team1 승, 1이면 team2 승, 2면 무승부, 경기 안했을 땐 null
 * @property[pointResult] 연장전까지의 라운드 포인트
 * @property[winTeam] 승리 팀의 별칭. 경기가 안 끝났으면 null
 * @property[roundCount] 진행할 세트 수
 * @property[winIdx] 승리 팀의 인덱스. 0이면 team1, 1이면 team2
 *
 * @constructor 새로운 경기 데이터 생성. [playNum]을 지정 가능
 */
data class Play(
	@ColumnInfo(name = "group_name") val group: String,
	val team1: String,
	val team2: String,
) {
	@ColumnInfo(name = "group_order")
	var order: Int = 1
	@ColumnInfo(name = "play_num")
	var playNum: Int = 0
	
	@ColumnInfo(name = "round_result")
	var roundResult: List<Int?> = listOf(null, null, null, null)
	
	//0: team1 승 / 1: team2 승 / 2: 무승부 / null: 경기 안함
	@ColumnInfo(name = "point_result")
	var pointResult: List<List<Int>> = List(4) { listOf(0, 0) }
	@ColumnInfo(name = "win_team")
	var winTeam: String? = null
	@ColumnInfo(name = "round_count")
	var roundCount: Int = 2
	
	val winIdx: Int?
		get() {
			return when (winTeam) {
				team1 -> 0
				team2 -> 1
				else -> null
			}
		}
	
	constructor(group: String, team1: String, team2: String, playNum: Int) :
					this(group, team1, team2) {
		this.playNum = playNum
	}
	
	
	/**
	 * 경기 점수를 초기화한다.
	 *
	 * @return [Team] 객체의 정보를 수정할 [ChangeData] 객체
	 */
	fun clear(): ChangeData {
		val changeData = ChangeData(this)
		
		roundResult = listOf(null, null, null, null)
		pointResult = List(4) { listOf(0, 0) }
		winTeam = null
		roundCount = 0
		
		changeData.changedData()
		
		return changeData
	}
	
	/**
	 * 경기 결과를 변경
	 *
	 * 포인트에 따라 이번 라운드의 승리 팀을 결정하고, 진행할 세트 수와 승리 팀을 재설정한다.
	 *
	 * @param[round] 변경할 라운드의 인덱스(1라운드 = 0)
	 * @param[point] [round]의 포인트. [[team1]의 포인트, [team2]의 포인트]
	 * @param[timeWin] 연장전 승리 팀의 인덱스. 0이면 team1, 1이면 team2, 연장전이 아니면 null
	 *
	 * @return [Team] 객체의 정보를 수정할 [ChangeData] 객체
	 */
	fun changeResult(round: Int, point: List<Int>, timeWin: Int? = null): ChangeData {
		val changeData = ChangeData(this)
		
		val newRoundResult = roundResult.toMutableList()
		val newPointResult = pointResult.toMutableList()
		
		
		if (point[0] > point[1]) { //팀1이 이번 세트 승리
			newRoundResult[round] = 0
		} else if (point[0] < point[1]) { //팀2가 이번 세트 승리
			newRoundResult[round] = 1
		} else { //이번 세트 무승부
			if (round == 3) //연장
				newRoundResult[round] = timeWin
			else
				newRoundResult[round] = 2
		}
		
		newPointResult[round] = point
		
		roundResult = newRoundResult.toList()
		pointResult = newPointResult.toList()
		
		//진행할 세트 수 변겅
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
		
		//승리 팀 재설정
		updateWinTeam()
		
		//정보를 수정하는 데 사용할 ChangeData 객체
		changeData.changedData()
		
		return changeData
	}
	
	/**
	 * 연장전이 필요한지 판단
	 *
	 * @return 연장전이 필요한지 여부
	 */
	private fun isNeedExtra(): Boolean {
		val round3Result = roundResult.subList(0, 3)
		val point3Result = pointResult.subList(0, 3)
		
		//이전 세트의 결과가 없으면 false, 양 팀의 승리 수가 다르면 false, 1:1인데 포인트가 같으면 true
		return if (round3Result[0] == null || round3Result[1] == null || round3Result[2] == null)
			false
		else if (round3Result.count { result -> result == 0 } != round3Result.count { result -> result == 1 })
			false
		else point3Result.sumOf { point -> point[0] } == point3Result.sumOf { point -> point[1] }
	}
	
	/**
	 * 3세트가 필요한지를 판단
	 *
	 * @return 3세트가 필요한지 여부
	 */
	private fun isNeed3(): Boolean {
		//1:1이면 true, 무승부가 존재하면 true
		return if (roundResult[0] == 0 && roundResult[1] == 1)
			true
		else if (roundResult[0] == 1 && roundResult[1] == 0)
			true
		else roundResult[0] == 2 || roundResult[1] == 2
	}
	
	/**
	 * 승리 팀을 결정하여 [winTeam] 수정
	 */
	private fun updateWinTeam() {
		when (roundCount) {
			2 -> {
				//두 라운드에서 승리한 팀 승리, 아니면 null
				winTeam = if (roundResult[0] == 0 && roundResult[1] == 0)
					team1
				else if (roundResult[0] == 1 && roundResult[1] == 1)
					team2
				else
					null
			}
			
			3 -> {
				//3라운드 결과가 없으면 null, 라운드 승리 수가 같으면 포인트 비교, 아니면 라운드 비교
				val round3Result = roundResult.subList(0, 3)
				val point3Result = pointResult.subList(0, 3)
				winTeam = if (roundResult[2] == null)
					null
				else if (round3Result.count { result -> result == 0 } == round3Result.count { result -> result == 1 }) {
					if (point3Result.sumOf { point -> point[0] } < point3Result.sumOf { point -> point[1] })
						team2
					else
						team1
				} else if (round3Result.count { result -> result == 0 } < round3Result.count { result -> result == 1 })
					team2
				else
					team1
			}
			
			4 -> {
				//연장전 이긴 팀이 승리 팀
				winTeam = when (roundResult[3]) {
					0 -> team1
					1 -> team2
					else -> null
				}
			}
		}
	}
}