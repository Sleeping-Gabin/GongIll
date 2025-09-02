package com.example.myapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team

@Dao
interface PlayDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertPlay(play: Play)
	
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertPlays(plays: List<Play>)
	
	@Update
	suspend fun updatePlay(play: Play)
	
	@Update
	suspend fun updatePlays(plays: List<Play>)
	
	@Delete
	suspend fun deletePlay(play: Play)
	
	@Query("SELECT * From play WHERE team1 LIKE :teamName OR team2 LIKE :teamName")
	suspend fun findByTeam(teamName: String): List<Play>
	
	@Query("SELECT MAX(play_num) From play WHERE team1 IN(:teams) AND team2 IN(:teams)")
	suspend fun getMaxPlayNum(teams: List<String>): Int
	
	@Query("SELECT MAX(group_order) FROM play WHERE group_name = :group")
	suspend fun getMaxOrder(group: String): Int
	
	@Query("SELECT play.* From play WHERE group_name = :group ORDER BY group_order")
	suspend fun findByGroup(group: String): List<Play>
	
	@Query("SELECT DISTINCT `groups`.*, play.* From play JOIN `groups` ON `groups`.name = play.group_name ORDER BY group_order")
	fun getGroupAndPlays(): LiveData<Map<Group, List<Play>>>
	
	@Query("SELECT DISTINCT team.*, play.* From play JOIN team ON team.group_name = play.group_name AND (team.alias = play.team1 OR team.alias = play.team2)")
	fun getTeamAndPlays(): LiveData<Map<Team, List<Play>>>
	
	@Query(
		"WITH c_play(team1, team2, win_team) AS (SELECT team1, team2, win_team FROM play " +
						"WHERE group_name = :group AND ((team1 = :team1 AND team2 = :team2) OR (team1 = :team2 AND team2 = :team1))) " +
						"SELECT (SELECT COUNT(*) From c_play WHERE win_team = :team2) " +
						"- (SELECT COUNT(*) From c_play WHERE win_team = :team1)"
	)
	suspend fun compareWin(group: String, team1: String, team2: String): Int
	
	@Query("SELECT * FROM play")
	fun getAllPlays(): LiveData<List<Play>>
}