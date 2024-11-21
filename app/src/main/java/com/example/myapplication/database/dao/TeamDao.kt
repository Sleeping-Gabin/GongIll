package com.example.myapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Team

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeam(team: Team): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeams(teams: List<Team>)

    @Update
    suspend fun updateTeam(team: Team)

    @Update
    suspend fun updateTeams(teamList: List<Team>)

    @Delete
    suspend fun deleteTeam(team: Team)

    @Query("SELECT * FROM team WHERE group_name = :groupName")
    suspend fun findByGroup(groupName: String): List<Team>

    @Query("SELECT groups.*, team.* FROM team JOIN groups ON groups.name = team.group_name")
    fun getGroupAndTeams(): LiveData<Map<Group, List<Team>>>

    @Query("SELECT group_name FROM team WHERE name = :teamName")
    suspend fun  getGroupName(teamName: String): String

    @Query("SELECT group_name FROM team WHERE alias = :alias")
    suspend fun getGroupNameWithAlias(alias: String): String

    @Query("SELECT * FROM team")
    fun getAllTeams(): LiveData<List<Team>>

    @Query("SELECT * FROM team WHERE group_name = :groupName AND alias = :teamAlias")
    suspend fun getTeam(groupName: String, teamAlias: String): Team

    @Query("UPDATE team SET name = :newName, alias = :newAlias WHERE group_name = :groupName AND name = :name")
    suspend fun changeName(groupName: String, name: String, newName: String, newAlias: String)

}