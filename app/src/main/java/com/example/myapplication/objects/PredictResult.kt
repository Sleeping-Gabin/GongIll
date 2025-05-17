package com.example.myapplication.objects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PredictResult(
	val teams: List<String>,
	var winScenario: List<Scenario>,
	var roundScenario: List<Scenario>,
	val reverse: Boolean
) : Parcelable