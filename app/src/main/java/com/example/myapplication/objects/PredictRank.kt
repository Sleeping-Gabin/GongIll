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
    private val finishedResult = playList.filter { it.winIdx != null }
        .map { GameResult(teams.indexOf(it.team1), teams.indexOf(it.team2), it.playNum, it.winIdx) } //진행한 경기
    private val remainPlay = playList.filter { it.winIdx == null }
        .map { GameResult(teams.indexOf(it.team1), teams.indexOf(it.team2), it.playNum, null) } //남은 경기

    private var winScenarios = hashSetOf<Scenario>()
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
        val targetWin = scenario.winNum(targetIdx)
        val highTeamCount = scenario.wins.count { it.value > targetWin } //타겟 팀보다 승리 수가 많은 팀 수

        if (highTeamCount >= targetRank) { //타겟 팀보다 승리 수가 많은 팀이 목표 순위보다 더 많으면 실패
            addScenario(scenario, "lose")
            return
        }

        val sameWinsCount = scenario.wins.count { it.value == targetWin } //타겟 팀과 승리 수가 같은 팀
        if (highTeamCount + sameWinsCount <= targetRank) {
            addScenario(scenario, "win")
            return
        }

        when (sameWinsCount) {
            2 -> {
                val sameWinTeamIdx = scenario.wins.entries.firstOrNull { it.value==targetWin && it.key!=targetIdx }?.key!!
                evaluateSingleSameWin(scenario, sameWinTeamIdx)
            }
            else -> addScenario(scenario, "round")
        }

        progress += (50f / (1 shl remainPlay.size)).toInt()
        update(progress)
    }

    private fun evaluateSingleSameWin(scenario: Scenario, opponentIdx: Int) {
        val targetTeamWins = (scenario.finishedResult + scenario.teamResults)
            .filter { (it.team1Idx == targetIdx && it.team2Idx == opponentIdx) || (it.team2Idx == targetIdx && it.team1Idx == opponentIdx) }
            .map { it.winner == targetIdx }

        val winCount = targetTeamWins.count { true }
        val loseCount = targetTeamWins.count { false }

        when {
            winCount > loseCount -> addScenario(scenario, "win")
            winCount < loseCount -> addScenario(scenario, "lose")
            else -> {
                val targetRnd = finishedRnd[targetIdx]
                val sameWinTeamRnd = finishedRnd[opponentIdx]
                val targetNewWin = scenario.newWinNum(targetIdx)
                val opponentNewWin = scenario.newWinNum(opponentIdx)

                if (targetRnd > sameWinTeamRnd + 4*opponentNewWin)
                    addScenario(scenario, "win")
                else if (sameWinTeamRnd > targetRnd + 4*targetNewWin)
                    addScenario(scenario, "lose")
                else
                    addScenario(scenario, "round")
            }
        }
    }

    private  fun addScenario(scenario: Scenario, type: String) {
        if (type == "win") {
            if (!reverse) winScenarios.add(scenario.copy())
        }
        else if (type == "lose") {
            if (reverse) winScenarios.add(scenario.copy())
        }
        else {
            roundScenarios.add(scenario.copy())
        }
    }

    fun predict(): PredictResult {
        exploreScenarios(Scenario(finishedResult, mutableListOf()), 0)

        if (winScenarios.size+roundScenarios.size > 1 shl remainPlay.size-1) {
            reverse = true
            winScenarios.clear()
            roundScenarios.clear()
            exploreScenarios(Scenario(finishedResult, mutableListOf()), 0)
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