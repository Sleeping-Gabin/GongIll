package com.example.myapplication.objects

import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team
import kotlin.math.min

/**
 * 경기 결과로 변경될 팀의 정보를 저장하는 클래스
 *
 * @param[play] 진행할 경기의 [Play] 객체
 *
 * @property[winChange] 승리 횟수, [Team.win] 변화. [[team1의 변화, team2의 변화]]
 * @property[roundChange] 라운드 승점, [Team.roundWin] 변화
 * @property[pointChange] 포인트 합계, [Team.point] 변화
 * @property[drawChange] 무승부 점수, [Team.drawRound] 변화
 * @property[countChange] 라운드 수, [Team.roundCount] 변화
 *
 * @constructor [play]로 인해 있었을 팀의 정보를 기록
 */
class ChangeData(val play: Play) {
	private var winChange = mutableListOf(0, 0)
	private var roundChange = mutableListOf(0, 0)
	private var pointChange = mutableListOf(0, 0)
	private var drawChange = mutableListOf(0, 0)
	private var countChange = mutableListOf(0, 0)
	
	init {
		previousData()
	}
	
	/**
	 * 클래스가 생성될 때 호출
	 *
	 * 변동되기 전 [play] 경기의 결과로 팀에 있었을 변화를 기록한다.
	 */
	private fun previousData() {
		//경기가 진행 중이면 변화 없음
		val winIdx = play.winIdx
		if (play.winIdx == null)
			return
		
		winChange[winIdx!!] -= 1 //기존 승리 팀의 승리 횟수 감소
		
		val round1 = play.roundResult.take(3).count { result -> result == 0 } //team1이 이긴 라운드 수 (연장 제외)
		val round2 = play.roundResult.take(3).count { result -> result == 1 } //team2가 이긴 라운드 수
		roundChange[0] -= round1 - round2 //라운드 승점(승리 라운드 수 - 패배 라운드 수) 제거
		roundChange[1] -= round2 - round1
		
		pointChange[0] -= play.pointResult.take(3).sumOf { result -> result[0] } //포인트 합계 (연장 제외) 제거
		pointChange[1] -= play.pointResult.take(3).sumOf { result -> result[1] }
		
		val roundDraw = play.roundResult.take(3).count { result -> result == 2 } //무승부 라운드 수
		drawChange[winIdx!!] += roundDraw //이긴 팀의 무승부 감소 복구
		drawChange[1 - winIdx!!] -= roundDraw //진 팀의 무승부 증가 복구
		
		countChange[0] -= min(play.roundCount, 3) //라운드 횟수 제거
		countChange[1] -= min(play.roundCount, 3)
	}
	
	/**
	 * [play]의 결과가 변동된 후 호출
	 *
	 * 경기 결과에 따라 변화할 팀의 정보를 기록한다.
	 */
	fun changedData() {
		//경기가 진행 중이면 변화 없음
		val winIdx = play.winIdx
		if (play.winIdx == null)
			return
		
		winChange[winIdx!!] += 1 //승리 팀의 승리 횟수 증가
		
		val round1 = play.roundResult.take(3).count { result -> result == 0 } //team1의 승리 라운드 수 (연장 제외)
		val round2 = play.roundResult.take(3).count { result -> result == 1 }  //team2의 승리 라운드 수
		roundChange[0] += round1 - round2 //실질 승리 수 (승리 라운드 수 - 패배 라운드 수)
		roundChange[1] += round2 - round1
		
		val roundDraw = play.roundResult.take(3).count { result -> result == 2 } //무승부 라운드 수
		pointChange[0] += play.pointResult.take(3)
			.sumOf { result -> result[0] } //team1이 얻은 포인트 합계 (연장 제외)
		pointChange[1] += play.pointResult.take(3).sumOf { result -> result[1] } //team2가 얻은 포인트 합계
		
		drawChange[winIdx!!] -= roundDraw //승리 팀의 무승부 수 감소
		drawChange[1 - winIdx!!] += roundDraw //패배 팀의 무승부 수 증가
		
		countChange[0] += min(play.roundCount, 3) //라운드 횟수 증가
		countChange[1] += min(play.roundCount, 3)
	}
	
	/**
	 * 팀의 정보를 변경
	 *
	 * @param[team1] [play]의 첫번째 팀
	 * @param[team2] [play]의 두번째 팀
	 */
	fun changeTeamInfo(team1: Team?, team2: Team?) {
		//잘못된 팀일 경우 변경하지 않음
		if (team1?.groupName != play.group || team2?.groupName != play.group)
			return
		else if (team1.alias != play.team1 || team2.alias != play.team2)
			return
		
		team1.apply {
			win += winChange[0]
			lose += winChange[1]
			roundWin += roundChange[0]
			point += pointChange[0]
			drawRound += drawChange[0]
			roundCount += countChange[0]
		}
		
		team2.apply {
			win += winChange[1]
			lose += winChange[0]
			roundWin += roundChange[1]
			point += pointChange[1]
			drawRound += drawChange[1]
			roundCount += countChange[1]
		}
	}
	
	
}