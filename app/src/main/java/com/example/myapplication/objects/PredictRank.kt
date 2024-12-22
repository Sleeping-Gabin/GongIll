package com.example.myapplication.objects

import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team


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
            val winIdx = if (it.winIdx==0) teams.indexOf(it.team1) else teams.indexOf(it.team2)
            GameResult(teams.indexOf(it.team1), teams.indexOf(it.team2), it.playNum,  winIdx)
        }
    private val remainPlay = playList.filter { it.winIdx == null }
        .map { GameResult(teams.indexOf(it.team1), teams.indexOf(it.team2), it.playNum, null) } //남은 경기

    private var winScenarios = hashSetOf<Scenario>()
    private var failScenario = hashSetOf<Scenario>()
    private var roundScenarios = hashSetOf<Scenario>()

    private var progress = 0

    private  var reverse = false

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

    private fun evaluateScenario(scenario: Scenario) {
        scenario.update()
        val targetWin = scenario.winNum(targetIdx) //타겟 팀 승리 수
        val highTeamCount = scenario.wins.count { it.value > targetWin } //타겟 팀보다 승리 수가 많은 팀 수
        
        val targetRnd = finishedRnd[targetIdx] //타겟 팀 라운드 승점
        val targetNewWin = scenario.newWinNum(targetIdx)
        val targetNewLose = scenario.newLoseNum(targetIdx)

        // 타겟 팀의 최대/최소 라운드 승점
        val targetMaxRnd = targetRnd + 2*targetNewWin
        val targetMinRnd = targetRnd - 2*targetNewLose

        val sameWinTeamIdxs = scenario.wins.entries //타겟 팀과 승리 횟수가 같은 팀들
                .filter { it.value==targetWin && it.key!=targetIdx }
                .map { it.key!! }
        val sameWinCount = sameWinTeamIdxs.size
        
        // 승리 횟수가 같은 팀의 최대/최소 라운드 승점
        val sameWinMaxRnds = sameWinTeamIdxs.map { finishedRnd[it] + 2*scenario.newWinNum(it) }
        val sameWinMinRnds = sameWinTeamIdxs.map { finishedRnd[it] - 2*scenario.newLoseNum(it) }
        
        // 최소 승점이 타겟 팀의 최대 승점보다 높다 -> 무조건 높은 순위
        // 최대 승점이 타겟 팀의 최소 승점보다 낮다 -> 무조건 낮은 순위
        val highRndTeamCount = sameWinMinRnds.count { it > targetMaxRnd }
        val lowRndTeamCount = sameWinMaxRnds.count { it < targetMinRnd }


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
                val sameWinTeamIdx = scenario.wins.entries.firstOrNull { it.value==targetWin && it.key!=targetIdx }?.key!!
                match1on1(scenario, sameWinTeamIdx)
            }
            else -> {
                addScenario(scenario, "round")
            }
        }

        progress += (50f / (1 shl remainPlay.size)).toInt()
        update(progress)
    }

    private fun match1on1(scenario: Scenario, opponentIdx: Int) {
        val targetTeamWins = (scenario.finishedResult + scenario.teamResults)
            .filter { (it.team1Idx == targetIdx && it.team2Idx == opponentIdx) || (it.team2Idx == targetIdx && it.team1Idx == opponentIdx) }
            .map { it.winner == targetIdx }

        val winCount = targetTeamWins.count { true }
        val loseCount = targetTeamWins.count { false }

        when {
            winCount > loseCount -> addScenario(scenario, "win")
            winCount < loseCount -> addScenario(scenario, "fail")
            else -> {
                addScenario(scenario, compareRound(scenario, opponentIdx))
            }
        }
    }

    private fun compareRound(scenario: Scenario, opponentIdx: Int): String {
        val targetRnd = finishedRnd[targetIdx]
        val targetNewWin = scenario.newWinNum(targetIdx)
        val targetNewLose = scenario.newLoseNum(targetIdx)

        val opponentRnd = finishedRnd[opponentIdx]
        val opponentNewWin = scenario.newWinNum(opponentIdx)
        val opponentNewLose = scenario.newLoseNum(opponentIdx)

        if (targetRnd - 2*targetNewLose > opponentRnd + 2*opponentNewWin)
            return "win"
        else if (opponentRnd - 2*opponentNewLose > targetRnd + 2*targetNewWin)
            return "fail"
        else
            return "round"
    }

    private  fun addScenario(scenario: Scenario, type: String) {
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

    fun predict(): PredictResult {
        exploreScenarios(Scenario(finishedResult, mutableListOf()), 0)

        if ((1 shl remainPlay.size)-winScenarios.size-roundScenarios.size < winScenarios.size ) { //성공 상황이 실패 상황보다 많을 때
            reverse = true
            winScenarios = failScenario.toHashSet()
        }

        progress = 50
        update(progress)

        var scenarios = winScenarios
        while (scenarios.isNotEmpty()) {
            scenarios = mergeDiffOne(scenarios, "win")

            progress += (25f / remainPlay.size).toInt()
            update(progress)
        }

        progress = 75
        update(75)

        scenarios = roundScenarios
        while (scenarios.isNotEmpty()) {
            scenarios = mergeDiffOne(scenarios, "round")

            progress += (25f / remainPlay.size).toInt()
            update(progress)
        }

        progress = 100
        update(100)

        return PredictResult(teams, winScenarios.toMutableList(), roundScenarios.toMutableList(), reverse)
    }

    private fun mergeDiffOne(scenarios: HashSet<Scenario>, type: String): HashSet<Scenario> {
        val scenariosToRemove = hashSetOf<Scenario>()
        val mergeScenarios = hashSetOf<Scenario>()

        for (scenario in scenarios) {
            for (other in scenarios) {
                if (scenario == other) continue //같은 시나리오면 패스

                val diffIdx = scenario.diffResultOne(other)
                if (diffIdx != -1) {
                    scenariosToRemove.add(scenario)
                    scenariosToRemove.add(other)

                    val mergeScenario = Scenario(finishedResult, scenario.teamResults.toMutableList()).apply {
                        teamResults.removeAt(diffIdx)
                    }
                    mergeScenarios.add(mergeScenario)
                }
            }
        }

        if (type == "win") {
            winScenarios.removeAll(scenariosToRemove)
            winScenarios.addAll(mergeScenarios)
        }
        else {
            roundScenarios.removeAll(scenariosToRemove)
            roundScenarios.addAll(mergeScenarios)
        }

        return mergeScenarios
    }
}