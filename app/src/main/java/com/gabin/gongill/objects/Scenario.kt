package com.gabin.gongill.objects

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 모든 경기가 진행됐을 때 도달할 수 있는 어떤 가능한 시나리오를 표현한 클래스
 *
 * @param[finishedResult] 이미 진행된 경기의 결과를 나타내는 [GameResult] 목록
 * @param[teamResults] 아직 완료되지 않은 경기의 가능한 결과를 나타내는 [GameResult] 목록
 *
 * @property[finishedWins] 진행된 경기에서 팀의 승리 횟수를 저장한 맵
 * @property[wins] 각 팀의 승리 횟수를 저장한 맵
 */
@Parcelize
data class Scenario(
	val finishedResult: List<GameResult>,
	val teamResults: MutableList<GameResult>
) : Parcelable {
	@IgnoredOnParcel
	private val finishedWins = finishedResult.groupingBy { it.winner }.eachCount()
	
	@IgnoredOnParcel
	var wins = mapOf<Int?, Int>()
	
	
	/**
	 * 경기 결과를 토대로 [wins]를 업데이트
	 */
	fun update() {
		wins = (finishedResult + teamResults).groupingBy { it.winner }.eachCount()
	}
	
	/**
	 * 팀의 승리 횟수를 반환
	 *
	 * @param[idx] 승리 횟수를 알고 싶은 팀의 인덱스
	 */
	fun winNum(idx: Int): Int {
		return wins.getOrDefault(idx, 0)
	}
	
	/**
	 * 이 시나리오 상에서 팀이 승리한 횟수를 반환.
	 * 전체 숭리 횟수 - 이미 진행된 경기의 승리 횟수
	 *
	 * @param[idx] 승리 횟수를 알고 싶은 팀의 인덱스
	 */
	fun newWinNum(idx: Int): Int {
		return winNum(idx) - finishedWins.getOrDefault(idx, 0)
	}
	
	/**
	 * 이 시나리오 상에서 팀이 패배한 횟수를 반환.
	 * 전체 패배 횟수 - 이미 진행된 경기의 패배 횟수
	 *
	 * @param[idx] 패배 횟수를 알고 싶은 팀의 인덱스
	 */
	fun newLoseNum(idx: Int): Int {
		return teamResults.count { it.team1Idx == idx || it.team2Idx == idx } - newWinNum(idx)
	}
	
	/**
	 * 두 시나리오가 포함한 [경기 결과][GameResult]가 같은지 여부를 반환
	 *
	 * @param[other] 비교할 [Scenario] 객체
	 *
	 * @return true이면 두 시나리오가 포함한 경기가 동일, false이면 다른 경기를 포함
	 */
	private fun isSameGames(other: Scenario): Boolean {
		if (other.teamResults.size != teamResults.size) return false
		for (i in 0 until teamResults.size) {
			if (!teamResults[i].isSameGame(other.teamResults[i])) return false
		}
		return true
	}
	
	/**
	 * 다른 시나리오와 경기 결과가 하나만 다를 경우 그 경기의 인덱스를,
	 * 그렇지 않을 경우 -1을 반환
	 *
	 * @param[other] 비교할 [Scenario] 객체
	 *
	 * @return 결과가 다른 경기의 인덱스 혹은 -1
	 */
	fun diffResultOne(other: Scenario): Int {
		//포함된 경기가 다른 경우
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
	
	/**
	 * 이 시나리오를 복사한 새로운 [Scenario] 객체를 반환
	 *
	 * @return 복사된 [Scenario] 객체
	 */
	fun copy(): Scenario {
		return Scenario(finishedResult, teamResults.toMutableList())
	}
}
