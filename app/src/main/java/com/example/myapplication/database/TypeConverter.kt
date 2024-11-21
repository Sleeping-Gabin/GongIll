package com.example.myapplication.database

import org.json.JSONArray

class TypeConverter {
    @androidx.room.TypeConverter
    fun ListToString(list: List<Int>): String {
        return JSONArray(list).toString()
    }

    @androidx.room.TypeConverter
    fun StringToList(string: String): List<Int> {
        val jsonArray = JSONArray(string)
        val list: List<Int> = Array(jsonArray.length()) {
            jsonArray.getInt(it)
        }.toList()
        return list
    }

    @androidx.room.TypeConverter
    fun ListNullToString(list: List<Int?>): String {
        return JSONArray(list).toString()
    }

    @androidx.room.TypeConverter
    fun StringToListNull(string: String): List<Int?> {
        val jsonArray = JSONArray(string)
        val list: List<Int?> = Array(jsonArray.length()) {
            if (jsonArray.isNull(it))
                null
            else
                jsonArray.getInt(it)
        }.toList()
        return list
    }

    @androidx.room.TypeConverter
    fun List2dToString(list: List<List<Int>>): String {
        val stringList: List<String> = list.map { list -> JSONArray(list).toString() }
        return JSONArray(stringList).toString()
    }

    @androidx.room.TypeConverter
    fun StringTList2d(string: String): List<List<Int>> {
        val jsonStringArray = JSONArray(string)
        val stringList: List<String> = Array(jsonStringArray.length()) {
            jsonStringArray.getString(it)
        }.toList()

        val list2d = mutableListOf<List<Int>>()
        for (s in stringList) {
            val jsonArray = JSONArray(s)
            val list: List<Int> = Array(jsonArray.length()) {
                jsonArray.getInt(it)
            }.toList()
            list2d.add(list)
        }
        return list2d.toList()
    }
}