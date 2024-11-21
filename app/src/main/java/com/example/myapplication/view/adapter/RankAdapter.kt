package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.entity.Team
import com.example.myapplication.databinding.RankItemBinding
import com.example.myapplication.view.listener.OnTeamTouchListener

class RankAdapter(tl: List<Team>?, val listener: OnTeamTouchListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var teamList: ArrayList<Team> = tl?.toCollection(ArrayList()) ?: arrayListOf()
    
    class ViewHolder(val binding: RankItemBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RankItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        val team = teamList[position]

        binding.rankRanking.text = team.rank.toString()
        binding.rankTeamName.text = team.alias
        binding.rankWin.text = team.win.toString()
        val point = team.point.toFloat()/team.roundCount.toFloat()
        binding.rankPoint.text = if (point.isNaN()) "0" else String.format("%.1f", point)
        binding.rankRndPt.text = team.roundWin.toString()

        binding.rankTeamInfo.setOnClickListener {
            listener.onTouchItem(team)
        }
    }

    fun changeData(list: List<Team>) {
        teamList = list.toCollection(ArrayList())
        notifyDataSetChanged()
    }
}