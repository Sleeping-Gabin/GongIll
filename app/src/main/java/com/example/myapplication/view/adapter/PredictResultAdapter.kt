package com.example.myapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.PredictResultItemBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.R

class PredictResultAdapter(result: PredictResult): RecyclerView.Adapter<PredictResultAdapter.ViewHolder>() {
    private val teams = result.teams
    private var winScenario = result.winScenario
    private var roundScenario = result.roundScenario

    private var resultList = result.winScenario

    enum class Mode { WIN, ROUND }
    class ViewHolder(val binding: PredictResultItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PredictResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.predictItemView.text = ""
        val binding = holder.binding
        val scenario = resultList[position]

        for (result in scenario.teamResults) {
            val team1 = teams[result.team1Idx]
            val team2 = teams[result.team2Idx]
            val winTeam = teams[result.winner!!]

            val str = holder.context.getString(R.string.predictResult, team1, team2, winTeam)
            if (binding.predictItemView.text.isNotEmpty())
                binding.predictItemView.append("\n")
            binding.predictItemView.append(str)
        }
    }

    fun changeMode(newMode: Mode) {
        resultList = when (newMode) {
            Mode.WIN -> {
                winScenario
            }

            Mode.ROUND -> {
                roundScenario
            }
        }
        notifyDataSetChanged()
    }

    fun clear() {
        resultList = listOf()
    }

    override fun getItemCount(): Int {
        //println(resultList.size)
        return resultList.size
    }
}