package com.example.myapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.database.dao.GroupDao
import com.example.myapplication.database.dao.PlayDao
import com.example.myapplication.database.dao.TeamDao
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Play
import com.example.myapplication.database.entity.Team

/*
@Database(entities = [Group::class, Team::class, Play::class], version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class MyDatabase: RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun teamDao(): TeamDao
    abstract fun playDao(): PlayDao

    /*
    companion objects {
        private var instance: MyDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MyDatabase? {
            if (instance == null) {
                synchronized(MyDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyDatabase::class.java,
                        "database"
                    ).addCallback(objects: Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val groupA = Group("A", 2)
                                getInstance(context)!!.groupDao().insertGroup(groupA)
                                val teamA = Team("AA", "A")
                                teamA.apply {
                                    win = 1
                                    point = 17
                                    roundCount = 3
                                    drawRound = -1
                                    roundWin = 0
                                }
                                getInstance(context)!!.teamDao().insertTeam(teamA)
                                val teamB = Team("BB", "A")
                                teamB.apply {
                                    lose = 1
                                    point = 9
                                    roundCount = 3
                                    drawRound = 1
                                    roundWin = 0
                                }
                                getInstance(context)!!.teamDao().insertTeam(teamB)
                                getInstance(context)!!.teamDao().insertTeam(Team("CC", "A"))
                                val play1 = Play("A", "AA", "BB", 1)
                                play1.winTeam = "AA"
                                play1.changeResult(0, listOf(4, 4))
                                play1.changeResult(1, listOf(10, 0))
                                play1.changeResult(2, listOf(3, 5))
                                getInstance(context)!!.playDao().insertPlay(play1)
                                val play2 = Play("A", "AA", "CC", 1)
                                play2.order = 2
                                getInstance(context)!!.playDao().insertPlay(play2)
                                val play3 = Play("A", "CC", "BB", 1)
                                play3.order = 3
                                getInstance(context)!!.playDao().insertPlay(play3)
                            }
                        }
                    }).build()
                }
            }
            return instance
        }
    }
    */
}
 */

@Database(entities = [Group::class, Team::class, Play::class], version = 2, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class MyDatabase : RoomDatabase() {
	abstract fun groupDao(): GroupDao
	abstract fun teamDao(): TeamDao
	abstract fun playDao(): PlayDao
}