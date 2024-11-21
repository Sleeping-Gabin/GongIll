package com.example.myapplication.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Play
import com.example.myapplication.databinding.PlayItemBinding
import com.example.myapplication.view.listener.OnPointTouchListener
import com.google.android.material.color.MaterialColors

class PlayAdapter(val play: Play, val listener: OnPointTouchListener): RecyclerView.Adapter<PlayAdapter.ViewHolder>() {
    class ViewHolder(val binding: PlayItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PlayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding

        val winColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorTertiaryContainer)
        val winTextColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnTertiaryContainer)
        val loseColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurfaceVariant)
        val textColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurfaceVariant)

        binding.set.text = if (position < 3)
                (position+1).toString() + " 세트"
        else
            "연장전"

        binding.team1Point.text = play.pointResult[position][0].toString()
        binding.team2Point.text = play.pointResult[position][1].toString()

        when(play.roundResult[position]) {
            0 -> { //team1 승
                binding.team1Point.setBackgroundColor(winColor)
                binding.team1Point.setTextColor(winTextColor)
                binding.team2Point.setBackgroundColor(loseColor)
                binding.team2Point.setTextColor(textColor)
            }
            1 -> { //team2 승
                binding.team2Point.setBackgroundColor(winColor)
                binding.team2Point.setTextColor(winTextColor)
                binding.team1Point.setBackgroundColor(loseColor)
                binding.team1Point.setTextColor(textColor)
            }
            else -> { //무승부 or 경기 안함
                binding.team1Point.setBackgroundColor(loseColor)
                binding.team1Point.setTextColor(textColor)
                binding.team2Point.setBackgroundColor(loseColor)
                binding.team2Point.setTextColor(textColor)
            }
        }

        binding.setPoint.setOnClickListener {
            listener.onTouchItem(play, position)
        }
    }

    override fun getItemCount(): Int {
        return play.roundCount
    }
}