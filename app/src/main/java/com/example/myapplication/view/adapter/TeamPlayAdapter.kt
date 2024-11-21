package com.example.myapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Play
import com.example.myapplication.databinding.TeamItemBinding

class TeamPlayAdapter(val team: String, playList: List<Play>?): RecyclerView.Adapter<TeamPlayAdapter.ViewHolder>() {
    private var playList: ArrayList<Play> = playList?.sortedBy{ it.order }?.toCollection(ArrayList()) ?: arrayListOf()

    class ViewHolder(val binding: TeamItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TeamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
    }

    override fun getItemCount(): Int {
        return playList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val play = playList[position]
        val teamIdx = if (play.team1 == team) 0 else 1

        binding.teamItemTeamName.text = if (play.team1 == team) play.team2 else play.team1

        for (rndTxt in binding.teamItemPlayInfo.children.withIndex()) {
            (rndTxt.value as TextView).text = if (play.roundResult[rndTxt.index] != null)
                String.format("%2d : %-2d", play.pointResult[rndTxt.index][teamIdx], play.pointResult[rndTxt.index][1-teamIdx])
            else
                "-"
        }

        if (play.playNum > 0) {
            binding.teamItemPlayNum.visibility = View.VISIBLE
            binding.teamItemPlayNum.text = play.playNum.toString()
        }
    }
}