package com.example.myapplication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.myapplication.database.MIGRATION_2_3
import com.example.myapplication.database.MyDatabase
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 데이터베이스와의 상호작용을 담당하는 Repository 클래스
 *
 * @param[application] 앱의 [Application] 객체
 *
 * @property[db] Room 데이터베이스 객체
 */
class CompetitionRepository(application: Application) {
	//private val db: MyDatabase? = MyDatabase.getInstance(application.applicationContext)
	
	private val db = Room.databaseBuilder(
		application.applicationContext, MyDatabase::class.java, "database"
	)
		.addMigrations(MIGRATION_2_3)
		.build()
	
	/**
	 * 데이터베이스의 카테고리 목록을 반환
	 *
	 * @return 카테고리 목록의 LiveData
	 */
	fun getCategoryList(): LiveData<List<String>> {
		return db.groupDao().getAllCategory()
	}
	
	/**
	 * 데이터베이스의 그룹 목록을 반환
	 *
	 * @return 그룹 목록의 LiveData
	 */
	fun getGroupList(): LiveData<List<Group>> {
		return db.groupDao().getAllGroups()
	}
	
	/**
	 * 데이터베이스의 팀 목록을 반환
	 *
	 * @return 팀 목록의 LiveData
	 */
	fun getTeamList(): LiveData<List<Team>> {
		return db.teamDao().getAllTeams()
	}
	
	/**
	 * 데이터베이스의 경기 목록을 반환
	 *
	 * @return 경기 목록의 LiveData
	 */
	fun getPlayList(): LiveData<List<Play>> {
		return db.playDao().getAllPlays()
	}
	
	/**
	 * 데이터베이스에 새로운 팀을 추가하고 다른 팀과의 경기 데이터를 생성한다.
	 *
	 * @param[newTeam] 추가할 [Team] 객체
	 *
	 * @return 추가된 팀의 ID
	 */
	suspend fun addTeam(newTeam: Team): Long {
		val group = db.groupDao().getGroup(newTeam.groupName)!! //그룹 이름
		val oldTeams = db.teamDao().findByGroup(newTeam.groupName) //그룹의 다른 팀들
		
		newTeam.rank = oldTeams.size + 1
		
		//데이터베이스에 팀 추가
		val result = db.teamDao().insertTeam(newTeam)
		if (result < 0) //팀 추가 실패
			return result
		
		//그룹의 경기의 마지막 순서 인덱스
		var maxOrder = db.playDao().getMaxOrder(group.name)
		
		//다른 팀과의 경기 데이터 추가
		if (group.playNum == 1) {
			for (team in oldTeams) {
				val newPlay = Play(group.name, team.alias, newTeam.alias)
				newPlay.order = maxOrder + 1
				maxOrder += 1
				db.playDao().insertPlay(newPlay)
			}
		} else {
			for (i in 1..group.playNum) {
				for (team in oldTeams) {
					val newPlay = Play(group.name, team.alias, newTeam.alias, i)
					newPlay.order = maxOrder + 1
					maxOrder += 1
					db.playDao().insertPlay(newPlay)
				}
			}
		}
		
		return result
	}
	
	/**
	 * 데이터베이스에 새 그룹을 추가한다.
	 *
	 * @param[newGroup] 추가할 [Group] 객체
	 *
	 * @return 추가된 그룹의 ID
	 */
	suspend fun addGroup(newGroup: Group): Long {
		val result = db.groupDao().insertGroup(newGroup)
		return result
	}
	
	/**
	 * 데이터베이스의 경기 데이터들을 업데이트한다.
	 *
	 * @param[changePlayList] 업데이트 할 [Play] 객체의 리스트
	 */
	fun updatePlays(changePlayList: List<Play>) {
		CoroutineScope(Dispatchers.IO).launch {
			db.playDao().updatePlays(changePlayList)
		}
	}
	
	/**
	 * 데이터베이스의 팀 데이터들을 업데이트한다.
	 *
	 * @param[changeTeamList] 업데이트 할 [Team] 객체의 리스트
	 */
	fun updateTeams(changeTeamList: List<Team>) {
		CoroutineScope(Dispatchers.IO).launch {
			db.teamDao().updateTeams(changeTeamList)
		}
	}
	
	/**
	 * 두 팀의 승리 수 차이를 반환
	 *
	 * @param[team1] 첫 번째 [Team] 객체
	 * @param[team2] 두 번째 [Team] 객체
	 *
	 * @return [team2]의 승리 수 - [team1]의 승리 수
	 */
	suspend fun whoWin(team1: Team, team2: Team): Int {
		return db.playDao().compareWin(team1.groupName, team1.alias, team2.alias)
	}
	
	/**
	 * 그룹의 카테고리와 이름 변경
	 *
	 * @param[group] 변경할 [Group] 객체
	 * @param[newCategory] 새로운 카테고리 이름
	 * @param[newName] 새로운 그룹 이름
	 */
	fun changeGroupCategoryAndName(group: Group, newCategory: String, newName: String) {
		CoroutineScope(Dispatchers.IO).launch {
			db.groupDao().changeName(group.name, newName)
			db.groupDao().changeCategory(group.name, newCategory)
		}
	}
	
	/**
	 * 데이터베이스에서 그룹 삭제
	 *
	 * @param[group] 삭제할 [Group] 객체
	 */
	fun deleteGroup(group: Group) {
		CoroutineScope(Dispatchers.IO).launch {
			db.groupDao().deleteGroup(group)
		}
	}
	
	/**
	 * 팀의 이름과 별칭 변경
	 *
	 * @param[team] 변경할 [Team] 객체
	 * @param[newName] 새로운 팀 이름
	 * @param[newAlias] 새로운 팀 별칭
	 */
	fun changeTeamName(team: Team, newName: String, newAlias: String) {
		CoroutineScope(Dispatchers.IO).launch {
			db.teamDao().changeName(team.groupName, team.name, newName, newAlias)
		}
	}
	
	/**
	 * 데이터베이스에서 팀 삭제
	 *
	 * @param[team] 삭제할 [Team] 객체
	 */
	fun deleteTeam(team: Team) {
		CoroutineScope(Dispatchers.IO).launch {
			db.teamDao().deleteTeam(team)
		}
	}
	
}
