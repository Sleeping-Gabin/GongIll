package com.example.myapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Team
import com.example.myapplication.databinding.GroupDetailItemBinding
import com.example.myapplication.view.listener.OnGroupTeamTouchListener
import com.example.myapplication.view.model.MyViewModel

class GroupTeamAdapter(teamList: List<Team>?, val listener: OnGroupTeamTouchListener, val model: MyViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var teamList: List<Team> = teamList?.toList() ?: listOf()

    class ViewHolder(val binding: GroupDetailItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root) { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(GroupDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        val team = teamList[position]

        val teamPlay = model.getTeamPlay(team)
        val finishPlaySize = teamPlay.count { p -> p.winTeam != null }

        binding.groupDetailItemTeamName.text = team.alias
        binding.groupDetailItemPlayInfo.text = holder.context.getString(R.string.groupPlayInfo, teamPlay.size, finishPlaySize)
        binding.root.setOnClickListener {
            listener.onTouchItem(team)
        }
    }

    fun changeData(newList: List<Team>?) {
        teamList = newList?.toList() ?: listOf()
        notifyDataSetChanged()
    }
}