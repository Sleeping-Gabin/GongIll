package com.example.myapplication.objects

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PredictResult(
    val targetIdx: Int,
    val teams: List<String>,
    val playInfo: List<Pair<Int, Int>>,
    val remainPlay: List<Pair<Int, Int>>,
    var winChance: MutableList<Pair<String, String>> = mutableListOf(),
    var roundChance: MutableList<Pair<String, String>> = mutableListOf(),
    var reverse: Boolean
): Parcelable