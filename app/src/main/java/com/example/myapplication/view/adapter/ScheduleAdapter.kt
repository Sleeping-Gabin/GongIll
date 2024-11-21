package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.view.listener.OnPlayDragListener
import com.example.myapplication.database.entity.Play
import com.example.myapplication.databinding.ScheduleItemBinding
import java.util.*
import kotlin.collections.ArrayList

class ScheduleAdapter(list: List<Play>?, val listener: OnPlayDragListener): RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {
    var playList: ArrayList<Play> = list?.toCollection(ArrayList()) ?: arrayListOf()
    private var filterList = ArrayList(playList)
    var filter = Filter.ALL

    enum class Filter { ALL, FINISH, YET }
    class ViewHolder(val binding: ScheduleItemBinding): RecyclerView.ViewHolder(binding.root) {
        var isSwiped = false
        init {
            binding.scheduleFrame.setOnClickListener {
                if (!isSwiped) {
                    if (binding.team1Score.visibility == View.VISIBLE) {
                        binding.team1Score.visibility = View.GONE
                        binding.drawScore.visibility = View.GONE
                        binding.team2Score.visibility = View.GONE
                    }
                    else {
                        binding.team1Score.visibility = View.VISIBLE
                        binding.drawScore.visibility = View.VISIBLE
                        binding.team2Score.visibility = View.VISIBLE
                    }
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val play = filterList[position]

        binding.team1.text = play.team1
        binding.team2.text = play.team2

        binding.team1Score.text = play.roundResult.count { result -> result==0 }.toString()
        binding.team1Score.visibility = View.GONE
        binding.team2Score.text = play.roundResult.count { result -> result==1 }.toString()
        binding.team2Score.visibility = View.GONE
        binding.drawScore.text = play.roundResult.count { result -> result==2 }.toString()
        binding.drawScore.visibility = View.GONE

        if (play.playNum > 0) {
            binding.playNum.text = play.playNum.toString()
            binding.playNum.visibility = View.VISIBLE
        }
        else
            binding.playNum.visibility = View.GONE

        binding.scheduleSwipe.setOnClickListener {
            listener.onTouchItem(play)
        }
    }

    fun dragItem(from: Int, to: Int) {
        val fromPlay = filterList[from]
        val toPlay = filterList[to]
        changeItemOrder(fromPlay, toPlay)

        val fromIdx = playList.indexOf(fromPlay)
        val toIdx = playList.indexOf(toPlay)
        Collections.swap(playList, fromIdx, toIdx)

        Collections.swap(filterList, from, to)
        notifyItemMoved(from, to)
        listener.onDragItem(fromPlay, toPlay)
    }

    private fun changeItemOrder(fromPlay: Play, toPlay: Play) {
        val temp = fromPlay.order
        fromPlay.order = toPlay.order
        toPlay.order = temp
    }

    fun changeData(newPlayList: List<Play>) {
        playList = newPlayList.toCollection(kotlin.collections.ArrayList())
        filterData()
    }

    fun filterData() {
        filterList = when (filter) {
            Filter.ALL -> ArrayList(playList)
            Filter.YET -> playList.filter { p -> p.winTeam==null }.toCollection(ArrayList())
            Filter.FINISH -> playList.filter { p -> p.winTeam!=null }.toCollection(ArrayList())
        }
        filterList.sortBy { it.order }
        notifyDataSetChanged()
    }
}