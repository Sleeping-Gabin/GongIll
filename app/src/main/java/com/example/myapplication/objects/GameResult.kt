package com.example.myapplication.objects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [PredictRank]에서 [Scenario]의 경기 결과를 나타내는 데이터 클래스
 *
 * @param[team1Idx] 첫번째 팀의 인덱스
 * @param[team2Idx] 두번째 팀의 인덱스
 * @param[playNum] team1과 team2 사이의 경기 순서. n이면 n번째 경기
 * @param[winner] 승리한 팀의 인덱스. null이면 아직 진행 중
 */
@Parcelize
data class GameResult(
	val team1Idx: Int,
	val team2Idx: Int,
	val playNum: Int,
	var winner: Int?
) : Parcelable {
	
	/**
	 * 두 [GameResult]가 나타내는 경기가 같은지 비교하는 함수
	 *
	 * @param[other] 비교할 [GameResult]
	 *
	 * @return 두 [GameResult]가 나타내는 경기가 같은 경우 true, 그렇지 않은 경우 false. [winner]는 상관 없음
	 */
	fun isSameGame(other: GameResult): Boolean {
		return team1Idx == other.team1Idx && team2Idx == other.team2Idx && playNum == other.playNum
	}
}