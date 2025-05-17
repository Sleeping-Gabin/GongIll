package com.example.myapplication.view.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.CompetitionRepository
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 앱의 데이터를 공유하는 ViewModel 클래스
 *
 * @property[database] 데이터베이스 사용을 위한 CompetitionRepository 객체
 * @property[toastObserver] 토스트 메시지를 띄우기 위한 LiveData 객체
 * @property[categoryList] 데이터베이스의 카테고리 목록의 LiveData 객체
 * @property[groupList] 데이터베이스의 그룹 목록의 LiveData 객체
 * @property[teamList] 데이터베이스의 팀 목록의 LiveData 객체
 * @property[playList] 데이터베이스의 경기 목록의 LiveData 객체
 * @property[groupTeamList] 그룹별 팀 목록의 LiveData 객체
 * @property[groupPlayList] 그룹별 경기 목록의 LiveData 객체
 * @property[teamPlayList] 팀별 경기 목록의 LiveData 객체
 * @property[categoryGroupList] 카테고리별 그룹 목록의 LiveData 객체
 * @property[selectedCurrentGroup] 앱에 표시되는 현재 선택된 그룹
 * @property[selectedGroup] 선택을 공유할 그룹
 * @property[selectedPlay] 선택을 공유할 경기
 * @property[selectedTeam] 선택을 공유할 팀
 * @property[currentTeamList] [selectedCurrentGroup]의 팀 목록
 * @property[currentPlayList] [selectedCurrentGroup] 의 경기 목록
 * @property[changePlayList] 변경된 경기의 목록
 * @property[changeTeamList] 변경된 팀의 목록
 *
 * @constructor 데이터베이스에서 데이터를 가져와 LiveData 객체에 저장
 */
class MyViewModel(application: Application): AndroidViewModel(application) {
    private val database: CompetitionRepository = CompetitionRepository(application)
    val toastObserver: MutableLiveData<String> = MutableLiveData()

    lateinit var categoryList: LiveData<List<String>>

    lateinit var groupList: LiveData<List<Group>>

    lateinit var teamList: LiveData<List<Team>>

    lateinit var playList: LiveData<List<Play>>

    val mediator = MediatorLiveData<Unit>()

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
        //데이터베이스에서 데이터를 가져와 LiveData 객체에 저장
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

        //LiveData가 변경될 때 관련된 변수들이 업데이트 되도록 함
        mediator.apply {
            addSource(categoryList) {
                updateCurrentTeamAndPlay()
                value = Unit
            }

            addSource(groupList) {
                updateGroupPlayList()
                updateGroupTeamList()
                updateCategoryGroupList()
                value = Unit
            }

            addSource(teamList) {
                updateTeamPlayList()
                updateGroupTeamList()
                updateCurrentTeamAndPlay()
                value = Unit
            }

            addSource(playList) {
                updateTeamPlayList()
                updateGroupPlayList()
                updateCurrentTeamAndPlay()
                rank()
                value = Unit
            }
        }
    }

    private var selectedCurrentGroup: Group? = null
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


    /**
     * 앱이 종료될 때 호출
     *
     *  데이터베이스를 업데이트한다.
     */
    override fun onCleared() {
        viewModelScope.launch {
            async { updateDatabase() }.await()
        }
        super.onCleared()
    }

    /**
     * [selectedCurrentGroup]을 선택된 그룹으로 변경하고,
     * [currentTeamList]와 [currentPlayList]를 업데이트한다.
     *
     * @param[name] 선택된 그룹의 이름
     */
    fun selectGroup(name: String) {
        selectedCurrentGroup = groupList.value?.find { it.name == name }
        updateCurrentTeamAndPlay()
    }

    /**
     * [selectedCurrentGroup]에 따라 [currentTeamList]와 [currentPlayList]를 업데이트한다.
     */
    private fun updateCurrentTeamAndPlay() {
        updateDatabase()
        _currentTeamList.value = groupTeamList.value?.get(selectedCurrentGroup) ?: listOf()
        _currentPlayList.value = groupPlayList.value?.get(selectedCurrentGroup) ?: listOf()
    }

    /**
     * [groupTeamList]를 업데이트한다.
     */
    private fun updateGroupTeamList() {
        //그룹에 따라 팀을 분류
        if (!groupList.value.isNullOrEmpty() && !teamList.value.isNullOrEmpty()) {
            _groupTeamList.value = teamList.value
                ?.filter { t -> groupList.value?.find { g -> g.name == t.groupName } != null }
                ?.groupBy { t -> groupList.value?.find { g -> g.name == t.groupName }!! }
        }
    }

    /**
     * [groupPlayList]를 업데이트한다.
     */
    private fun updateGroupPlayList() {
        //그룹에 따라 경기를 분류
        if (!groupList.value.isNullOrEmpty() && !playList.value.isNullOrEmpty()) {
            _groupPlayList.value = playList.value
                ?.filter { p -> groupList.value?.find { g -> g.name == p.group } != null }
                ?.groupBy { p -> groupList.value?.find { g -> g.name == p.group }!! }
        }
    }

    /**
     * [teamPlayList]를 업데이트한다.
     */
    private fun updateTeamPlayList() {
        //팀에 따라 팀이 속한 경기를 분류
        if (!playList.value.isNullOrEmpty() && !teamList.value.isNullOrEmpty()) {
            _teamPlayList.value = teamList.value?.associateWith {
                t -> playList.value?.filter {
                    p -> p.group == t.groupName && (t.alias == p.team1 || t.alias == p.team2) } ?: listOf() }
        }
    }

    /**
     * [categoryGroupList]를 업데이트
     */
    private fun updateCategoryGroupList() {
        //카테고리에 따라 그룹을 분류
        if (!groupList.value.isNullOrEmpty() && !categoryList.value.isNullOrEmpty()) {
            _categoryGroupList.value = groupList.value?.groupBy { g ->
                categoryList.value?.find { c -> c == g.category } ?: "others"
            }
        }
    }


    /**
     * 데이터베이스에 새로운 팀 추가
     *
     * @param[groupName] [Team.groupName]에 해당하는 팀이 속한 그룹의 이름
     * @param[teamName] [Team.name]에 해당하는 팀 이름
     * @param[teamAlias] [Team.alias]에 해당하는 팀의 별칭
     */
    fun addTeam(groupName: String, teamName: String, teamAlias: String) {
        viewModelScope.launch {
            val newTeam = Team(teamName, groupName, teamAlias)
            val result = CoroutineScope(Dispatchers.IO).async { database.addTeam(newTeam) }
            if (result.await() < 0) //이미 동일한 팀이 존재하면 추가하지 않음
                toastObserver.value = "이미 존재하는 팀입니다."
        }
    }

    /**
     * [changePlayList]와 [changeTeamList]의 데이터를 데이터베이스에 업데이트
     */
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

    /**
     * [selectedTeam]이 속한 경기의 목록을 반환
     *
     * @return [selectedTeam]이 속한 경기의 List
     */
    fun getTeamPlay(): List<Play>? {
        return teamPlayList.value?.get(selectedTeam)
    }

    /**
     *[team]이 속한 경기 목록을 반환
     *
     * @param[team] 경기 목록을 원하는 팀의 [Team] 객체
     *
     * @return [team]이 속한 경기 List
     */
    fun getTeamPlay(team: Team): List<Play> {
        return teamPlayList.value?.get(team) ?: listOf()
    }

    /**
     * 팀의 별칭으로 해당 팀의 객체를 반환
     *
     * @param[alias] 원하는 팀의 별칭
     *
     * @return 별칭이 [alias]인 팀의 [Team] 객체
     */
    fun getTeamWithAlias(alias: String): Team {
        return teamList.value?.find { it.groupName == selectedCurrentGroup?.name && it.alias == alias }!!
    }

    /**
     * 데이터베이스에 새로운 그룹 추가
     *
     * 카테고리로 "others"를 사용하거나 해당 그룹이 이미 존재할 시 추가되지 않는다.
     *
     * @param[groupName] [Group.name]에 해당하는 그룹 이름
     * @param[playNum] [Group.playNum]에 해당하는 같은 팀간의 경기 횟수
     * @param[category] [Group.category]에 해당하는 그룹의 카테고리 이름
     */
    fun addGroup(groupName: String, playNum: Int, category: String) {
        viewModelScope.launch {
            //카테고리가 없을 때 사용하는 "others"를 카테고리 이름으로 사용하지 못하게 함
            if (category == "others") {
                toastObserver.value = "해당 카테고리 이름은 사용할 수 없습니다."
                cancel()
            }

            val newGroup = Group(groupName, playNum)
            if (category.isNotBlank())
                newGroup.category = category

            val result = CoroutineScope(Dispatchers.IO).async { database.addGroup(newGroup) }
                if (result.await() < 0) //이미 동일한 그룹이 존재하면 추가하지 않음
                    toastObserver.value = "이미 존재하는 그룹입니다."
        }
    }

    /**
     * [teamList]의 팀의 순위를 계산
     *
     * 순위는 승리 수 (-> 승패) -> 라운드 승점 -> 포인트 -> 무승부 순으로 결정된다.
     *
     * @param[teamList] 순위를 계산할 팀의 List. default는 [currentTeamList]
     */
    fun rank(teamList: List<Team> = currentTeamList.value?: listOf()) {
        //경기 기록이 있는 경우 승리 수 -> 패배 수에 따라 분리해서 정렬 (승리 수가 같을 시 패배가 적은 팀이 높은 등수)
        val rankedList = teamList.filter { it.roundCount > 0 }
            .groupByTo(sortedMapOf(compareByDescending { it })) { it.win }.values
            .map { list -> list.groupByTo(sortedMapOf(compareBy { it })) { it.lose }.values }.flatten()

        rankedList.forEach { list -> //승리 수와 패배 수가 같은 팀들의 리스트
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

        //각 Team 객체에 순위 저장
        rankedList.flatten().withIndex().forEach { it.value.rank = it.index + 1 }

        //경기 기록이 없으면 뒷 순위에 배치
        teamList.filter { it.roundCount == 0 }
            .withIndex().forEach { it.value.rank = teamList.size - it.index }

       changeTeamList.addAll(teamList)
    }

    /**
     * 데이터베이스에서 그룹 삭제
     *
     * @param[group] 삭제할 [Group] 객체
     */
    fun deleteGroup(group: Group) {
        database.deleteGroup(group)
    }

    /**
     * 데이터베이스에서 그룹의 카테고리와 이름 변경
     *
     * "others"는 카테고리로 사용할 수 없다.
     *
     * @param[group] 카테고리와 이름을 바꿀 [Group] 객체
     * @param[category] [Group.category]에 해당하는 변경할 카테고리
     * @param[groupName] [Group.name]에 해당하는 변경할 이름
     */
    fun changeGroupCategoryAndName(group: Group, category:String, groupName: String) {
        if (category == "others") //카테고리가 없을 때 사용하는 "others"를 카테고리 이름으로 사용하지 못하게 함
            toastObserver.value = "해당 카테고리 이름은 사용할 수 없습니다."
        else if (category.isBlank())
            database.changeGroupCategoryAndName(group, "others", groupName)
        else
            database.changeGroupCategoryAndName(group, category, groupName)
    }

    /**
     * 데이터베이스에서 팀의 이름과 별칭 변경
     *
     * @param[team] 변경할 [Team] 객체
     * @param[teamName] [Team.name]에 해당하는 변경할 이름
     * @param[teamAlias] [Team.alias]에 해당하는 변경할 별칭
     */
    fun changeTeamName(team: Team, teamName: String, teamAlias: String) {
        database.changeTeamName(team, teamName, teamAlias)
    }

    /**
     * 데이터베이스에서 팀과 팀의 경기 데이터 삭제
     *
     * @param[team] 삭제할 [Team] 객체
     */
    fun deleteTeam(team: Team) {
        //team이 포함된 경기 리스트
        val playList = getTeamPlay() ?: listOf()

        for (play in playList) {
            //team과 경기한 팀의 경기 데이터 초기화
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

    /**
     * [group]의 경기 목록을 반환
     *
     * @param[group] 경기 목록을 원하는 그룹의 [Group] 객체
     *
     * @return [group]의 경기 List
     */
    fun getGroupPlay(group: Group): List<Play> {
        return groupPlayList.value?.get(group) ?: listOf()
    }

    /**
     * [group]의 팀 목록을 반환
     *
     * @param[group] 팀 목록을 원하는 그룹의 [Group] 객체
     *
     * @return [group]의 팀 List
     */
    fun getGroupTeam(group: Group): List<Team> {
        return groupTeamList.value?.get(group) ?: listOf()
    }
}