package com.example.myapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.PredictResultItemBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.R

class PredictResultAdapter(result: PredictResult): RecyclerView.Adapter<PredictResultAdapter.ViewHolder>() {
    private val targetIdx = result.targetIdx
    private val teams = result.teams
    private val playInfo = result.playInfo
    private val remainPlay = result.remainPlay
    private var winChance = result.winChance
    private var roundChance = result.roundChance

    private var resultList = result.winChance

    enum class Mode { WIN, ROUND }
    class ViewHolder(val binding: PredictResultItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PredictResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val chance = resultList[position]

        binding.predictItemView.text = ""
        for (c in chance.first.withIndex()) {
            val team1 = teams[targetIdx]
            val team2 = teams[remainPlay[c.index].first]
            val winTeam = if (c.value == '1') team1 else if (c.value == '0') team2 else continue

            if (binding.predictItemView.text.isNotEmpty())
                binding.predictItemView.append("\n")
            val str = holder.context.getString(R.string.predictResult, team1, team2, winTeam)
            binding.predictItemView.append(str)
        }

        for (c in chance.second.withIndex()) {
            val team1 = teams[playInfo[c.index].first]
            val team2 = teams[playInfo[c.index].second]
            val winTeam = if (c.value == '0') team1 else if (c.value == '1') team2 else continue

            val str = holder.context.getString(R.string.predictResult, team1, team2, winTeam)
            if (binding.predictItemView.text.isNotEmpty())
                binding.predictItemView.append("\n")
            binding.predictItemView.append(str)
        }
    }

    fun changeMode(newMode: Mode) {
        resultList = when (newMode) {
            Mode.WIN -> {
                winChance
            }

            Mode.ROUND -> {
                roundChance
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        println(resultList)
        return resultList.size
    }
}