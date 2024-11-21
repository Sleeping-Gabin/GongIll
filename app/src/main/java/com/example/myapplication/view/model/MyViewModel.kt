package com.example.myapplication.view.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.myapplication.CompetitionRepository
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MyViewModel(application: Application): AndroidViewModel(application) {
    private val database: CompetitionRepository = CompetitionRepository(application)
    val toastObserver: MutableLiveData<String> = MutableLiveData()

    lateinit var categoryList: LiveData<List<String>>

    lateinit var groupList: LiveData<List<Group>>

    lateinit var teamList: LiveData<List<Team>>

    lateinit var playList: LiveData<List<Play>>

    private var _groupTeamList: MutableLiveData<Map<Group, List<Team>>> = MutableLiveData()
    val groupTeamList: LiveData<Map<Group, List<Team>>>
        get() = _groupTeamList

    private var _groupPlayList: MutableLiveData<Map<Group, List<Play>>> = MutableLiveData()
    val groupPlayList: LiveData<Map<Group, List<Play>>>
        get() = _groupPlayList

    private var _teamPlayList: MutableLiveData<Map<Team, List<Play>>> = MutableLiveData()
    val teamPlayList: LiveData<Map<Team, List<Play>>>
        get() = _teamPlayList

    private var _categoryGroupList: MutableLiveData<Map<String, List<Group>>> = MutableLiveData()
    val categoryGroupList: LiveData<Map<String, List<Group>>>
        get() = _categoryGroupList


    init {
        viewModelScope.launch {
            val categoryListJob = async { database.getCategoryList() }
            val groupListJob = async { database.getGroupList() }
            val teamListJob = async { database.getTeamList() }
            val playListJob = async { database.getPlayList() }

            categoryList = categoryListJob.await()
            groupList = groupListJob.await()
            teamList = teamListJob.await()
            playList = playListJob.await()
        }
    }

    var selectedCurrentGroup: Group? = null
    var selectedGroup: Group? = null
    var selectedPlay: Play? = null
    var selectedTeam: Team? = null

    private var _currentTeamList = MutableLiveData<List<Team>>()
    val currentTeamList: LiveData<List<Team>>
        get() = _currentTeamList

    private var _currentPlayList = MutableLiveData<List<Play>>()
    val currentPlayList: LiveData<List<Play>>
        get() = _currentPlayList


    val changePlayList: ArrayList<Play> = arrayListOf()
    val changeTeamList: ArrayList<Team> = arrayListOf()


    override fun onCleared() {
        viewModelScope.launch {
            async { updateDatabase() }.await()
        }
        super.onCleared()
    }

    fun selectGroup(name: String) {
        selectedCurrentGroup = groupList.value?.find { it.name == name }
        updateCurrentTeamAndPlay()
    }

    fun updateCurrentTeamAndPlay() {
        updateDatabase()
        _currentTeamList.value = groupTeamList.value?.get(selectedCurrentGroup) ?: listOf()
        _currentPlayList.value = groupPlayList.value?.get(selectedCurrentGroup) ?: listOf()
    }

    fun updateGroupTeamList() {
        if (!groupList.value.isNullOrEmpty() && !teamList.value.isNullOrEmpty()) {
            _groupTeamList.value = teamList.value?.groupBy { t ->
                groupList.value?.find { g -> g.name == t.groupName }!!
            }
        }
    }

    fun updateGroupPlayList() {
        if (!groupList.value.isNullOrEmpty() && !playList.value.isNullOrEmpty()) {
            _groupPlayList.value = playList.value?.groupBy { p ->
                groupList.value?.find { g -> g.name == p.group }!!
            }
        }
    }

    fun updateTeamPlayList() {
        if (!playList.value.isNullOrEmpty() && !teamList.value.isNullOrEmpty()) {
            _teamPlayList.value = teamList.value?.associateWith {
                t -> playList.value?.filter {
                    p -> p.group == t.groupName && (t.alias == p.team1 || t.alias == p.team2) } ?: listOf() }
        }
    }

    fun updateCategoryGroupList() {
        if (!groupList.value.isNullOrEmpty() && !categoryList.value.isNullOrEmpty()) {
            _categoryGroupList.value = groupList.value?.groupBy { g ->
                categoryList.value?.find { c -> c == g.category } ?: "others"
            }
        }
    }

    fun addTeam(groupName: String, teamName: String, teamAlias: String) {
        viewModelScope.launch {
            val newTeam = Team(teamName, groupName, teamAlias)
            val result = CoroutineScope(Dispatchers.IO).async { database.addTeam(newTeam) }
            if (result.await() < 0)
                toastObserver.value = "이미 존재하는 팀입니다."
        }
    }

    fun updateDatabase() {
        if (changePlayList.isNotEmpty()) {
            database.updatePlays(changePlayList.toList())
            changePlayList.clear()
        }
        if (changeTeamList.isNotEmpty()) {
            database.updateTeams(changeTeamList.toList())
            changeTeamList.clear()
        }
    }

    fun getTeamPlay(): List<Play>? {
        return teamPlayList.value?.get(selectedTeam)
    }

    fun getTeamPlay(team: Team): List<Play> {
        return teamPlayList.value?.get(team) ?: listOf()
    }

    fun getTeamWithAlias(alias: String): Team {
        return teamList.value?.find { it.groupName == selectedCurrentGroup?.name && it.alias == alias }!!
    }

    fun addGroup(groupName: String, playNum: Int, category: String) {
        viewModelScope.launch {
            if (category == "others") {
                toastObserver.value = "해당 카테고리 이름은 사용할 수 없습니다."
                cancel()
            }

            val newGroup = Group(groupName, playNum)
            if (category.isNotBlank())
                newGroup.category = category

            val result = CoroutineScope(Dispatchers.IO).async { database.addGroup(newGroup) }
                if (result.await() < 0)
                    toastObserver.value = "이미 존재하는 그룹입니다."
        }
    }

    fun rank(teamList: List<Team> = currentTeamList.value?: listOf()) {
        //승리 수 (-> 승패) -> 라운드 승점 -> 포인트 -> 무승부
        //경기 기록이 있는 경우 승리 수 + 패배 수에 따라 분리해서 정렬 (승리 수가 같을 시 패배가 적은 팀이 높은 등수)
        val rankedList = teamList.filter { it.roundCount > 0 }
            .groupByTo(sortedMapOf(compareByDescending { it })) { it.win }.values
            .map { list -> list.groupByTo(sortedMapOf(compareBy { it })) { it.lose }.values }.flatten()

        rankedList.forEach { list ->
            if (list.size == 2) { //승리 수가 같은 팀이 두 팀이면 승패 -> 라운드 승점 -> 포인트 -> 무승부 순으로 정렬
                list.sortWith( Comparator<Team>  { team1, team2 -> runBlocking { database.whoWin(team1, team2) } }
                    .thenByDescending { it.roundWin }
                    .thenByDescending { it.point.toFloat() / it.roundCount }
                    .thenByDescending { it.drawRound })

            }
            else { //승리 수가 같은 팀이 3팀 이상이면 라운드 승점 -> 포인트 -> 무승부 순으로 정렬
                list.sortWith(compareByDescending<Team> { it.roundWin }
                    .thenByDescending { it.point.toFloat() / it.roundCount }
                    .thenByDescending { it.drawRound })
            }
        }

        rankedList.flatten().withIndex().forEach { it.value.rank = it.index + 1 }

        //경기 기록이 없으면 뒷 순위에 배치
        teamList.filter { it.roundCount == 0 }
            .withIndex().forEach { it.value.rank = teamList.size - it.index }

       changeTeamList.addAll(teamList)
    }

    fun deleteGroup(group: Group) {
        database.deleteGroup(group)
    }

    fun changeGroupCategoryAndName(group: Group, category:String, groupName: String) {
        if (category == "others")
            toastObserver.value = "해당 카테고리 이름은 사용할 수 없습니다."
        else if (category.isBlank())
            database.changeGroupCategoryAndName(group, "others", groupName)
        else
            database.changeGroupCategoryAndName(group, category, groupName)
    }

    fun changeTeamName(team: Team, teamName: String, teamAlias: String) {
        database.changeTeamName(team, teamName, teamAlias)
    }

    fun deleteTeam(team: Team) {
        val playList = getTeamPlay() ?: listOf()
        for (play in playList) {
            val changeData = play.clear()
            val team1 = teamList.value?.find { it.alias == play.team1 && it.groupName == team.groupName }
            val team2 = teamList.value?.find { it.alias == play.team2 && it.groupName == team.groupName }
            changeData.changeTeamInfo(team1, team2)

            changePlayList.add(play)
            if (play.team1 == team.alias && team2 != null)
                changeTeamList.add(team2)
            else if (team1 != null)
                changeTeamList.add(team1)
        }
        database.deleteTeam(team)
    }

    fun getGroupPlay(group: Group): List<Play> {
        return groupPlayList.value?.get(group) ?: listOf()
    }

    fun getGroupTeam(group: Group): List<Team> {
        return groupTeamList.value?.get(group) ?: listOf()
    }
}