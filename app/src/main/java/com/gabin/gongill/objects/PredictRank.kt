package com.gabin.gongill.objects

import com.gabin.gongill.database.entity.Play
import com.gabin.gongill.database.entity.Team


/**
 * 응원하는 팀이 목표 순위에 들 수 있는 시나리오를 찾는 클래스
 *
 * @param[teamList] 팀 목록
 * @param[playList] 경기 목록
 * @param[targetTeam] 목표 팀, 응원하는 팀의 별칭
 * @param[targetRank] 목표 순위
 * @param[update] 진행 바를 업데이트 하는 함수
 *
 * @property[teams] 팀의 별칭 목록
 * @property[targetIdx] 응원하는 팀의 [teams]에서의 인덱스
 * @property[finishedRnd] 각 팀의 라운드 승점 목록
 * @property[finishedResult] 진행 완료한 경기의 [GameResult] 객체 리스트
 * @property[remainPlay] 진행이 끝나지 않은 경기의 [GameResult] 객체 리스트
 * @property[winScenarios] 확정적인 목표 달성 [시나리오][Scenario]의 집합
 * @property[failScenario] 확정적인 목표 달성 실패 [시나리오][Scenario]의 집합
 * @property[roundScenarios] 라운드 비교가 필요한 [시나리오][Scenario]의 집합
 * @property[progress] 현재 진행 상황. 0 ~ 100
 * @property[reverse] true이면 목표 달성 실패 시나리오 탐색, false이면 목표 달성 시나리오 탐색
 */
class PredictRank(
	teamList: List<Team>,
	playList: List<Play>,
	targetTeam: String,
	private val targetRank: Int,
	private val update: (Int) -> Unit
) {
	private val teams = teamList.map { it.alias }
	private val targetIdx = teams.indexOf(targetTeam)
	private val finishedRnd = teamList.map { it.roundWin }
	private val finishedResult = playList.filter { it.winIdx != null } //진행한 경기
		.map {
			val winIdx = if (it.winIdx == 0) teams.indexOf(it.team1) else teams.indexOf(it.team2)
			GameResult(teams.indexOf(it.team1), teams.indexOf(it.team2), it.playNum, winIdx)
		}
	private val remainPlay = playList.filter { it.winIdx == null }
		.map { GameResult(teams.indexOf(it.team1), teams.indexOf(it.team2), it.playNum, null) } //남은 경기
	
	private var winScenarios = hashSetOf<Scenario>()
	private var failScenario = hashSetOf<Scenario>()
	private var roundScenarios = hashSetOf<Scenario>()
	
	private var progress = 0
	
	private var reverse = false
	
	
	/**
	 * 가능한 모든 게임 결과 시나리오를 재귀적으로 탐색한다.
	 *
	 * 각 경기에 대해 가능한 모든 결과(팀1 승리 또는 팀2 승리)를 탐색하여 남은 경기의 결과를 시뮬레이션한다.
	 * 시나리오 트리를 순회하기 위해 깊이 우선 탐색(depth-first search) 방식을 사용한다.
	 *
	 * [depth]가 [remainPlay]의 크기와 같으면 모든 남은 경기를 고려했음을 의미하므로,
	 * [evaluateScenario] 함수로 현재 시나리오에서의 최종 순위를 평가한다.
	 *
	 * 그렇지 않은 경우, 다음 경기에서 팀1이 이기는 결과와 팀2가 이기는 결과를 각각 시나리오에 추가하고
	 * 재귀적으로 [exploreScenarios]를 호출한다.
	 *
	 * @param[scenario] 현재 탐색 중인 시나리오. 지금까지 누적된 경기 결과 목록을 포함.
	 * @param[depth] 현재 탐색 깊이. [remainPlay] 리스트에서 고려 중인 경기의 인덱스.
	 */
	private fun exploreScenarios(
		scenario: Scenario,
		depth: Int
	) {
		if (depth == remainPlay.size) {
			evaluateScenario(scenario)
			return
		}
		
		val team1Idx = remainPlay[depth].team1Idx
		val team2Idx = remainPlay[depth].team2Idx
		
		// 팀1이 이기는 경우
		scenario.teamResults.add(GameResult(team1Idx, team2Idx, remainPlay[depth].playNum, team1Idx))
		exploreScenarios(scenario, depth + 1)
		scenario.teamResults.removeAt(depth)
		
		// 팀2가 이기는 경우
		scenario.teamResults.add(GameResult(team1Idx, team2Idx, remainPlay[depth].playNum, team2Idx))
		exploreScenarios(scenario, depth + 1)
		scenario.teamResults.removeAt(depth)
	}
	
	
	/**
	 * 주어진 시나리오를 평가하여 목표 팀이 원하는 순위를 달성할 수 있는지 판단한다.
	 *
	 * 1. 목표 팀보다 순위가 높은/낮은 팀 확인
	 *   - 목표 팀과 승리 횟수가 동일한 팀들의 최대/최소 라운드 승점을 계산하고,
	 *     이를 기반으로 목표 팀보다 순위가 높거나 낮다고 확정된 팀을 찾는다.
	 * 2. 순위를 기반으로 성공/실패 판단
	 *   - 목표 팀보다 확실히 순위가 높은 팀의 수가 목표 순위 이상이면 시나리오는 "실패"한다.
	 *   - 목표 팀보다 확실히 순위가 높은 팀의 수 + 라운드 비교를 통해 높은 순위가 될 가능성이 있는 팀의 수가 목표 순위보다 작으면 시나리오는 "성공"한다.
	 * 3. 1대1 비교가 필요한 상황 확인
	 *   - 승리 횟수가 같은 팀이 하나뿐인 경우, [1대1 비교][match1on1]를 실행해 달성 여부를 판단한다.
	 *   - 승리 횟수가 같은 팀이 여러 팀이며 점수로 명확한 순위를 결정할 수 없는 경우, 시나리오는 "라운드 비교"로 표시한다.
	 *
	 * @param[scenario] 평가할 [시나리오][Scenario]
	 */
	private fun evaluateScenario(scenario: Scenario) {
		scenario.update()
		
		val targetWin = scenario.winNum(targetIdx) //타겟 팀 승리 수
		val highTeamCount = scenario.wins.count { it.value > targetWin } //타겟 팀보다 승리 수가 많은 팀 수
		
		val targetRnd = finishedRnd[targetIdx] //타겟 팀 라운드 승점
		val targetNewWin = scenario.newWinNum(targetIdx)
		val targetNewLose = scenario.newLoseNum(targetIdx)
		
		// 타겟 팀의 최대/최소 라운드 승점
		val targetMaxRnd = targetRnd + 2 * targetNewWin
		val targetMinRnd = targetRnd - 2 * targetNewLose
		
		val sameWinTeamIdxs = scenario.wins.entries //타겟 팀과 승리 횟수가 같은 팀들
			.filter { it.value == targetWin && it.key != targetIdx }
			.map { it.key!! }
		val sameWinCount = sameWinTeamIdxs.size //타겟 팀과 승리 횟수가 같은 팀의 수
		
		// 승리 횟수가 같은 팀의 최대/최소 라운드 승점
		val sameWinMaxRnds = sameWinTeamIdxs.map { finishedRnd[it] + 2 * scenario.newWinNum(it) }
		val sameWinMinRnds = sameWinTeamIdxs.map { finishedRnd[it] - 2 * scenario.newLoseNum(it) }
		
		// 최소 승점이 타겟 팀의 최대 승점보다 높다 -> 무조건 높은 순위
		// 최대 승점이 타겟 팀의 최소 승점보다 낮다 -> 무조건 낮은 순위
		var highRndTeamCount = sameWinMinRnds.count { it > targetMaxRnd }
		var lowRndTeamCount = sameWinMaxRnds.count { it < targetMinRnd }
		
		// 승리 수가 같은 팀이 하나면 승점 비교의 의미 없음
		if (sameWinCount == 1) {
			highRndTeamCount = 0
			lowRndTeamCount = 0
		}
		
		// 타겟 팀보다 높은 순위의 팀이 목표 순위보다 더 많으면 실패
		if (highTeamCount + highRndTeamCount >= targetRank) {
			addScenario(scenario, "fail")
			return
		}
		
		// 승리 수가 같거나 많은 팀이 목표 순위보다 더 적으면 성공
		if (highTeamCount + sameWinCount - lowRndTeamCount < targetRank) {
			addScenario(scenario, "win")
			return
		}
		
		when (sameWinCount) {
			1 -> { //승리 수가 같은 팀이 하나일 때 두 팀간의 승패로 순위 결정
				val sameWinTeamIdx =
					scenario.wins.entries.firstOrNull { it.value == targetWin && it.key != targetIdx }?.key!!
				match1on1(scenario, sameWinTeamIdx)
			}
			
			else -> {
				addScenario(scenario, "round")
			}
		}
		
		progress += (20f / (1 shl remainPlay.size)).toInt()
		update(progress)
	}
	
	
	/**
	 * 승리 횟수가 같은 팀이 하나일 때, 성공/실패/라운드 비교 판단
	 *
	 * 두 팀 사이의 경기 결과, 승리 횟수가 많은 팀이 더 높은 순위가 된다.
	 * 목표 팀이 순위가 높을 경우 "성공"하고, 상대 팀이 순위가 높을 경우 "실패"한다.
	 * 승리 수가 같다면 [라운드 승패로 판단][compareRound]한다.
	 *
	 * @param[scenario] 현재 시나리오
	 * @param[opponentIdx] 상대 팀의 인덱스
	 */
	private fun match1on1(scenario: Scenario, opponentIdx: Int) {
		val targetTeamWins = (scenario.finishedResult + scenario.teamResults)
			.filter { (it.team1Idx == targetIdx && it.team2Idx == opponentIdx) || (it.team2Idx == targetIdx && it.team1Idx == opponentIdx) }
			.map { it.winner == targetIdx }
		
		val winCount = targetTeamWins.count { it }
		val loseCount = targetTeamWins.count { !it }
		
		when {
			//목표 팀의 승리 수가 많으면 성공
			winCount > loseCount -> addScenario(scenario, "win")
			//목표 팀의 승리 수가 적으면 실패
			winCount < loseCount -> addScenario(scenario, "fail")
			else -> {
				addScenario(scenario, compareRound(scenario, opponentIdx))
			}
		}
	}
	
	/**
	 * 승리 횟수가 같은 팀이 하나이고, 두 팀 사이의 승리 횟수도 같을 때, 성공/실패/라운드 비교를 판단한다.
	 * 목표 팀의 최소 라운드 승점 > 상대 팀의 최대 라운드 승점이면 "성공'하고,
	 * 상대 팀의 최소 라운드 승점 > 목표 팀의 최대 라운드 승점이면 "실패"한다.
	 *
	 * @param[scenario] 현재 시나리오
	 * @param[opponentIdx] 상대 팀의 인덱스
	 */
	private fun compareRound(scenario: Scenario, opponentIdx: Int): String {
		val targetRnd = finishedRnd[targetIdx] //목표 팀의 라운드 승점
		val targetNewWin = scenario.newWinNum(targetIdx) //목표 팀의 시나리오 상의 승리
		val targetNewLose = scenario.newLoseNum(targetIdx) //목표 팀의 시나리오 상의 패배
		
		val opponentRnd = finishedRnd[opponentIdx] //상대 팀의 라운드 승점
		val opponentNewWin = scenario.newWinNum(opponentIdx) //상대 팀의 시나리오 상의 승리
		val opponentNewLose = scenario.newLoseNum(opponentIdx) //상대 팀의 시나리오 상의 패배
		
		return if (targetRnd - 2 * targetNewLose > opponentRnd + 2 * opponentNewWin)
		//목표 팀의 최소 라운드 승점 > 상대 팀의 최대 라운드 승점
			"win"
		else if (opponentRnd - 2 * opponentNewLose > targetRnd + 2 * targetNewWin)
		//상대 팀의 최소 라운드 승점 > 목표 팀의 최대 라운드 승점
			"fail"
		else
			"round"
	}
	
	/**
	 * 시나리오 집합에 시나리오 추가
	 *
	 * @param[scenario] 추가할 새로운 [시나리오][Scenario]
	 * @param[type] 처리할 시나리오 유형. "win"이면 [winScenarios]에 추가,
	 *  "fail"이면 [failScenario]에 추가, "round"이면 [roundScenarios]에 추가
	 */
	private fun addScenario(scenario: Scenario, type: String) {
		when (type) {
			"win" -> {
				winScenarios.add(scenario.copy())
			}
			
			"fail" -> {
				failScenario.add(scenario.copy())
			}
			
			else -> {
				roundScenarios.add(scenario.copy())
			}
		}
	}
	
	/**
	 * 가능한 모든 시나리오를 탐색하고, 성공/라운드 비교 시나리오를 식별하며,
	 * 유사한 시나리오들을 병합하여 간결한 예측 결과를 제공
	 *
	 * 1. 남은 경기 결과를 기반으로 가능한 모든 결과를 탐색한다.
	 * 2. 성공 시나리오가 실패 시나리오보다 많다면 [reverse]를 true로 하여 패배 시나리오를 성공으로 간주하도록 한다.
	 * 3. 단 하나의 경기 결과만 다른 성공 시나리오와 라운드 비교 시나리오들을 반복적으로 [병합][mergeDiffOne]한다.
	 * 5. 팀 목록, 성공 시나리오 목록, 라운드 비교 시나리오 목록,
	 *    결과가 반전되었는지 여부를 나타내는 플래그를 포함하는 [PredictResult] 객체를 생성
	 *
	 * @return 결과를 담은 [PredictResult] 객체
	 */
	fun predict(): PredictResult {
		//시나리오 탐색
		exploreScenarios(Scenario(finishedResult, mutableListOf()), 0)
		
		//성공 상황이 실패 상황보다 많으면 실패 상황을 성공 상황으로 간주
		if ((1 shl remainPlay.size) - winScenarios.size - roundScenarios.size < winScenarios.size) {
			reverse = true
			winScenarios = failScenario.toHashSet()
		}
		
		progress = 20
		update(progress)
		
		//경기 결과가 하나 차이나는 성공 시나리오 병합
		var scenarios = winScenarios
		while (scenarios.isNotEmpty()) {
			scenarios = mergeDiffOne(scenarios, "win")
			
			progress += (40f / remainPlay.size).toInt()
			update(progress)
		}
		
		progress = 60
		update(progress)
		
		//경기 결과가 하나 차이나는 라운드 비교 시나리오 병합
		scenarios = roundScenarios
		while (scenarios.isNotEmpty()) {
			scenarios = mergeDiffOne(scenarios, "round")
			
			progress += (40f / remainPlay.size).toInt()
			update(progress)
		}
		
		progress = 100
		update(progress)
		
		return PredictResult(
			teams,
			winScenarios.toMutableList(),
			roundScenarios.toMutableList(),
			reverse
		)
	}
	
	/**
	 * 경기 결과가 하나만 다른 시나리오들을 병합
	 *
	 * 시나리오 집합을 순회하며 하나의 경기 결과만 다른 두 시나리오를 식별한다.
	 * 두 시나리오를 제거하고, 차이나는 경기 결과를 제거한 새로운 병합된 시나리오를 추가한다.
	 *
	 * @param[scenarios] 처리할 시나리오 집합
	 * @param[type] 처리할 시나리오 유형. "win"인 경우 [winScenarios]에서 병합, 그렇지 않으면 [roundScenarios]에서 병합
	 * @return 병합된 시나리오를 포함하는 새로운 집합
	 *
	 * @see Scenario
	 * @see Scenario.diffResultOne
	 */
	private fun mergeDiffOne(scenarios: HashSet<Scenario>, type: String): HashSet<Scenario> {
		val scenariosToRemove = hashSetOf<Scenario>() //제거할 시나리오 집합
		val mergeScenarios = hashSetOf<Scenario>() //병합된 시나리오 집합
		
		for (scenario in scenarios) {
			for (other in scenarios) {
				if (scenario == other) continue //같은 시나리오면 패스
				
				val diffIdx = scenario.diffResultOne(other) //차이나는 경기 결과가 하나일 때, 그 경기의 인덱스
				if (diffIdx != -1) {
					scenariosToRemove.add(scenario)
					scenariosToRemove.add(other)
					
					val mergeScenario = Scenario(finishedResult, scenario.teamResults.toMutableList()).apply {
						//차이나는 경기 결과 제거
						teamResults.removeAt(diffIdx)
					}
					mergeScenarios.add(mergeScenario)
				}
			}
		}
		
		if (type == "win") {
			winScenarios.removeAll(scenariosToRemove)
			winScenarios.addAll(mergeScenarios)
		} else {
			roundScenarios.removeAll(scenariosToRemove)
			roundScenarios.addAll(mergeScenarios)
		}
		
		return mergeScenarios
	}
}