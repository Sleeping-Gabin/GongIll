package com.example.myapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Group
import com.example.myapplication.databinding.GroupItemBinding
import com.example.myapplication.databinding.GroupItemHeaderBinding
import com.example.myapplication.view.listener.OnGroupTouchListener
import com.example.myapplication.view.model.MyViewModel

class GroupAdapter(val category: String, groupList: List<Group>?, val listener: OnGroupTouchListener, val model: MyViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var groupList = groupList ?: listOf()
    class ViewHolder(val binding: GroupItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root) {

    }

    class HeaderViewHolder(val binding: GroupItemHeaderBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0)
            ViewHolder(GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
        else //header
            HeaderViewHolder(GroupItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> { //일반
                val binding = (holder as ViewHolder).binding
                val group = groupList[position-1]

                binding.groupItemTeam.text = group.name

                val allPlay = model.getGroupPlay(group)
                val finishPlaySize = allPlay.count { p -> p.winTeam != null }
                binding.groupItemDescription.text = holder.context.getString(R.string.groupPlayInfo, allPlay.size, finishPlaySize)

                binding.root.setOnClickListener {
                    listener.onTouchItem(group)
                }

                binding.groupItemAddTeamButton.setOnClickListener {
                    listener.onTouchButton(group)
                }
            }
            1 -> { //header
                val binding = (holder as HeaderViewHolder).binding
                binding.addBtn.setOnClickListener {
                    listener.onTouchHeader(category)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return groupList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 1 else 0
    }

    fun changeData(list: List<Group>) {
        groupList = list
        notifyDataSetChanged()
    }
}