package com.example.myapplication.objects

import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team
import kotlin.math.pow

class PredictRank(teamList: List<Team>, playList: List<Play>, targetTeam: String, val targetRank: Int, val update: (Int) -> Unit) {
    private val teams = teamList.map { it.alias }
    private val targetIdx = teams.indexOf(targetTeam)
    private val currentWin = teamList.map { it.win } //현재 팀별 승리 횟수
    private val currentRank = teamList.map { it.rank } //현재 팀별 순위
    val playInfo: List<Pair<Int, Int>> //타겟 팀이 참여 하지 않는 남은 경기. (팀1 index, 팀2 index)
    private val currentTargetWinInfo: Map<Pair<Int, Int>, Boolean?> //(i, j): 타겟 팀이 teams[i]팀과의 j번째 경기에서 이겼으면 true 졌으면 false 기록이 없으면 null
    val remainPlay: List<Pair<Int, Int>> //(상대 팀 index, playnum)

    var reverse = false
    var winChance: MutableList<Pair<String, String>> = mutableListOf()
    var roundChance: MutableList<Pair<String, String>> = mutableListOf()

    private var progress = 0

    init {
        playInfo = playList.filter { it.winIdx == null && it.team1 != targetTeam && it.team2 != targetTeam}
            .map { Pair(teams.indexOf(it.team1), teams.indexOf(it.team2)) }

        currentTargetWinInfo =
            playList.filter { it.team1 == targetTeam || it.team2 == targetTeam }
                .associate {
                    if (it.team1 == targetTeam) {
                        Pair(Pair(teams.indexOf(it.team2), it.playNum), it.winTeam?.equals(targetTeam))
                    } else {
                        Pair(Pair(teams.indexOf(it.team1), it.playNum), it.winTeam?.equals(targetTeam))
                    }
                }

        remainPlay = currentTargetWinInfo.filter { it.value == null }.keys.toList()
    }

    inner class Chance(private val targetBinary: String, private val binary: String,
                       wins: List<Int>, targetWinInfo: Map<Pair<Int, Int>, Boolean?>) {
        private val wins = mutableListOf<Int>()
        private val targetWinInfo = mutableMapOf<Pair<Int, Int>, Boolean?>()

        init {
            this.wins.addAll(wins)
            this.targetWinInfo.putAll(targetWinInfo)
            setInfo() //이번 가능성에 따른 승리 수와 승리 정보 변경
        }

        private fun setInfo() {
            //이번 가능성에 따른 승리 수와 승리 정보 변경
            for (c in binary.withIndex()) {
                if (c.value == '0')
                    wins[playInfo[c.index].first] += 1
                else
                    wins[playInfo[c.index].second] += 1
            }
        }

        fun predict() {
            val highTeam = wins.count { it > wins[targetIdx] } //타겟 팀보다 승리 수가 많은 팀 수
            if (highTeam >= targetRank) return //타겟 팀보다 승리 수가 많은 팀이 목표 순위보다 더 많으면 실패

            val same = wins.count { it == wins[targetIdx] } //타겟 팀과 승리 수가 같은 팀 수
            if (highTeam + same <= targetRank) { //승리 수가 같은 팀 다 목표 순위 내에 든다면 성공
                winChance.add(Pair(targetBinary, binary))
                return
            }

            when (same) {
                0 -> { winChance.add(Pair(targetBinary, binary)) } //타겟 팀과 승리 수가 같은 팀이 없으면 성공
                1 -> { //타겟 팀과 승리 수가 같은 팀이 한 팀이면 두 팀 중 한 팀만 목표 순위 내에 포함
                    //승패 비교
                    val result = targetWinInfo.filter { it.key.first == wins.indexOfFirst { it == wins[targetIdx] } }
                    if (result.count { it.value == true } > result.count { it.value == false }) //승리가 많으면 성공
                        winChance.add(Pair(targetBinary, binary))
                    else if (result.count { it.value == true } == result.count { it.value == false }) //승패가 동일하면 라운드 비교
                        roundChance.add(Pair(targetBinary, binary))
                    else  //패배가 많으면 실패
                        return
                }
                else -> { roundChance.add(Pair(targetBinary, binary)) } //타겟 팀과 승리 수가 같은 팀이 두 팀 이상이면 라운드 비교
            }
        }
    }


    fun predict(): PredictResult {
        for (i in 0 until 2f.pow(remainPlay.size).toInt()) { //타겟 팀의 남은 경기에 따라 가능성 분류
            val targetBinary = if (remainPlay.isEmpty()) ""
                else String.format("%0${remainPlay.size}d", Integer.toBinaryString(i).toInt()) //001 -> 패패승

            if (targetBinary.count { it == '1' }  + currentWin[targetIdx]
                < currentWin[currentRank.indexOfFirst { it == targetRank }])
                continue //타겟 팀의 승리 수가 현재 목표 순위인 팀의 승리 수 이상이 아니라면 실패

            val wins = mutableListOf<Int>()
            val targetWinInfo = mutableMapOf<Pair<Int, Int>, Boolean?>()

            wins.addAll(currentWin)
            targetWinInfo.putAll(targetWinInfo)

            for (c in targetBinary.withIndex()) { //이번 가능성에 따른 승리 수와 승리 정보 변경
                if (c.value == '0') {
                    wins[remainPlay[c.index].first] += 1
                    targetWinInfo[remainPlay[c.index]] = false
                }
                else {
                    wins[targetIdx] += 1
                    targetWinInfo[remainPlay[c.index]] = true
                }
            }

            for (j in 0 until 2f.pow(playInfo.size).toInt()) { //경기 결과에 따라 가능성 확인
                val binary = if (playInfo.isEmpty()) ""
                    else String.format("%0${playInfo.size}d", Integer.toBinaryString(j).toInt())

                val chance = Chance(targetBinary, binary, wins, targetWinInfo)
                chance.predict()

                progress += (1f / 2f.pow((remainPlay.size + playInfo.size)) * 50).toInt()
                update(progress)
            }
        }

        progress = 50
        update(progress)
        arrange() //승리 가능성 정리
        progress = 100
        update(progress)

        return PredictResult(targetIdx, teams, playInfo, remainPlay, winChance, roundChance, reverse)
    }

    private fun arrange() {
        val result = mutableListOf<String>()
        var winList = winChance.map { it.first + it.second }.toMutableList()
        var roundlist = roundChance.map { it.first + it.second }.toMutableList()

        //확정 승리 가능성 성리
        //계산을 줄이기 위해 이기는 조건, 지는 조건 중 더 적은 쪽을 계산
        if (winList.size > 2f.pow(remainPlay.size + playInfo.size - 1)) {
            reverse = true
            val newList = mutableListOf<String>()
            for (i in 0 until 2f.pow(remainPlay.size + playInfo.size).toInt()) {
                val binary = String.format("%0${remainPlay.size + playInfo.size}d", Integer.toBinaryString(i).toInt())
                if (!winList.contains(binary) && !roundlist.contains(binary))
                    newList.add(binary)
            }
            winList = newList
        }

        //승패가 관계 없는 경기 제거 (1100, 1000 -> 두번째 경기의 승패는 관계 없으므로 1-00)
        var newList: MutableList<String>
        for (i in 0 until  remainPlay.size + playInfo.size) { //경기 수만큼 반복
            newList = compose(winList)
            result.addAll(winList)
            winList = newList

            progress += (1f / (remainPlay.size + playInfo.size) * 20).toInt()
            update(progress)
        }
        result.addAll(winList)
        
        winChance = result.mapTo(mutableListOf()) { s ->
            Pair(s.substring(0, winChance[0].first.length), s.substring(winChance[0].first.length, s.length)) }

        progress = 70
        update(progress)


        //라운드 결과에 따른 승리 가능성 정리
        result.clear()

        //승패가 관계 없는 경기 제거
        for (i in 0 until  remainPlay.size + playInfo.size) {
            newList = compose(roundlist)
            result.addAll(roundlist)
            roundlist = newList
            progress += (1f / (remainPlay.size + playInfo.size) * 20).toInt()
            update(progress)
        }
        result.addAll(roundlist)

        roundChance = result.mapTo(mutableListOf()) { s ->
            Pair(s.substring(0, roundChance[0].first.length), s.substring(roundChance[0].first.length, s.length)) }
    }

    private fun compose(list: MutableList<String>): MutableList<String> {
        println(list)
        val nextList = mutableListOf<String>()
        val used = nextList.associateWithTo(mutableMapOf()) { false }

        for (s in list.withIndex()) {
            for (idx2 in s.index+1 until list.size) {
                val s1 = s.value
                val s2 = list[idx2]
                val diff = s1.withIndex().count { it.value != s2[it.index] }
                if (diff == 1) { //한 경기의 승패만 다르다면 두 경기의 결과를 합함 (1100, 1000 -> 1-00)
                    used[s1] = true
                    used[s2] = true
                    val newString = s1.withIndex().map { if (it.value == s2[it.index]) it.value else "-" }.joinToString("")
                    if (!nextList.contains(newString)) nextList.add(newString)
                }
            }
        }
        list.removeAll { used[it] == true } //합해지지 않은 경기만 남김

        return nextList
    }
}