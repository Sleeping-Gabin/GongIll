package com.gabin.gongill.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gabin.gongill.database.dao.GroupDao
import com.gabin.gongill.database.dao.PlayDao
import com.gabin.gongill.database.dao.TeamDao
import com.gabin.gongill.database.entity.Group
import com.gabin.gongill.database.entity.Play
import com.gabin.gongill.database.entity.Team

val MIGRATION_2_3 = object : Migration(2, 3) {
	override fun migrate(db: SupportSQLiteDatabase) {
		db.execSQL("ALTER TABLE groups ADD COLUMN rule TEXT NOT NULL DEFAULT 'COA7'")
	}
}

@Database(entities = [Group::class, Team::class, Play::class], version = 3, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class MyDatabase : RoomDatabase() {
	abstract fun groupDao(): GroupDao
	abstract fun teamDao(): TeamDao
	abstract fun playDao(): PlayDao
}