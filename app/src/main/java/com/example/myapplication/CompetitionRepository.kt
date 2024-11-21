package com.example.myapplication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.myapplication.database.MyDatabase
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompetitionRepository(application: Application) {
    //private val db: MyDatabase? = MyDatabase.getInstance(application.applicationContext)

    private val db = Room.databaseBuilder(
            application.applicationContext, MyDatabase::class.java, "database")
        .build()

    fun getCategoryList(): LiveData<List<String>> {
        return db.groupDao().getAllCategory()
    }

    fun getGroupList(): LiveData<List<Group>> {
        return db.groupDao().getAllGroups()
    }

    fun getTeamList(): LiveData<List<Team>> {
        return db.teamDao().getAllTeams()
    }

    fun getPlayList(): LiveData<List<Play>> {
        return db.playDao().getAllPlays()
    }

    suspend fun addTeam(newTeam: Team): Long {
        val group = db.groupDao().getGroup(newTeam.groupName)!!
        val oldTeams = db.teamDao().findByGroup(newTeam.groupName)

        newTeam.rank = oldTeams.size + 1

        val result = db.teamDao().insertTeam(newTeam)
        if (result < 0)
            return result

        var maxOrder = db.playDao().getMaxOrder(group.name)

        if (group.playNum == 1) {
            for (team in oldTeams) {
                val newPlay = Play(group.name, team.alias, newTeam.alias)
                newPlay.order = maxOrder + 1
                maxOrder += 1
                db.playDao().insertPlay(newPlay)
            }
        }
        else {
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

    suspend fun addGroup(newGroup: Group): Long {
        val result = db.groupDao().insertGroup(newGroup)
        return result
    }

    fun updatePlays(changePlayList: List<Play>) {
        CoroutineScope(Dispatchers.IO).launch {
            db.playDao().updatePlays(changePlayList)
        }
    }

    fun updateTeams(changeTeamList: List<Team>) {
        CoroutineScope(Dispatchers.IO).launch {
            db.teamDao().updateTeams(changeTeamList)
        }
    }

    suspend fun whoWin(team1: Team, team2:Team): Int {
        return db.playDao().compareWin(team1.groupName, team1.alias, team2.alias)
    }

    fun changeGroupCategoryAndName(group: Group, newCategory: String, newName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.groupDao().changeName(group.name, newName)
            db.groupDao().changeCategory(group.name, newCategory)
        }
    }

    fun deleteGroup(group: Group) {
        CoroutineScope(Dispatchers.IO).launch {
            db.groupDao().deleteGroup(group)
        }
    }

    fun changeTeamName(team: Team, newName: String, newAlias: String) {
        CoroutineScope(Dispatchers.IO).launch {
            db.teamDao().changeName(team.groupName, team.name, newName, newAlias)
        }
    }

    fun deleteTeam(team: Team) {
        CoroutineScope(Dispatchers.IO).launch {
            db.teamDao().deleteTeam(team)
        }
    }

}
