package com.example.myapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.database.entity.Group

@Dao
interface GroupDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertGroup(group: Group): Long
	
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertGroups(groups: List<Group>)
	
	//@Update
	//suspend fun updateGroup(group: Group)
	
	@Query("UPDATE groups SET name = :newName WHERE name = :oldName")
	suspend fun changeName(oldName: String, newName: String)
	
	@Query("UPDATE groups SET category = :newCategory WHERE name = :groupName")
	suspend fun changeCategory(groupName: String, newCategory: String)
	
	@Delete
	suspend fun deleteGroup(group: Group)
	
	@Query("SELECT * FROM groups")
	fun getAllGroups(): LiveData<List<Group>>
	
	@Query("SELECT * FROM groups WHERE name LIKE :name")
	suspend fun getGroup(name: String): Group?
	
	@Query("SELECT category, * FROM groups")
	fun getCategoryAndGroups(): LiveData<Map<@MapColumn(columnName = "category") String, List<Group>>>
	
	@Query("SELECT DISTINCT category FROM groups")
	fun getAllCategory(): LiveData<List<String>>
}