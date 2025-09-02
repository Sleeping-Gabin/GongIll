package com.gabin.gongill.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gabin.gongill.database.entity.Play
import com.gabin.gongill.databinding.PlayItemBinding
import com.gabin.gongill.view.listener.OnPointTouchListener
import com.google.android.material.color.MaterialColors

/**
 * 경기 결과를 보여주는 Adapter
 *
 * @param[play] 경기 결과
 * @param[listener] 세트를 터치했을 때 호출되는 [OnPointTouchListener]
 */
class PlayAdapter(val play: Play, val listener: OnPointTouchListener) :
	RecyclerView.Adapter<PlayAdapter.ViewHolder>() {
	
	/**
	 * 세트 아이템의 ViewHolder
	 *
	 * @param[binding] PlayItemBinding 객체
	 * @param[context] 부모의 Context 객체
	 */
	class ViewHolder(val binding: PlayItemBinding, val context: Context) :
		RecyclerView.ViewHolder(binding.root)
	
	/**
	 * ViewHolder가 생성될 때 호출
	 *
	 * @param[parent] 부모 ViewGroup 객체
	 * @param[viewType] View의 타입
	 *
	 * @return 생성된 ViewHolder
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			PlayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
			parent.context
		)
	}
	
	/**
	 * ViewHolder가 연결될 때 호출
	 *
	 * 세트의 승리 팀에 따라 점수 칸의 색을 지정하고 터치 이벤트의 listener을 추가한다.
	 *
	 * @param[holder] 연결할 ViewHolder 객체
	 * @param[position] ViewHolder의 위치
	 */
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val binding = holder.binding
		
		//승리 팀 점수 칸 색
		val winColor = MaterialColors.getColor(
			binding.root,
			com.google.android.material.R.attr.colorTertiaryContainer
		)
		val winTextColor = MaterialColors.getColor(
			binding.root,
			com.google.android.material.R.attr.colorOnTertiaryContainer
		)
		
		//패배 팀 점수 칸 색
		val loseColor =
			MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurfaceVariant)
		val textColor = MaterialColors.getColor(
			binding.root,
			com.google.android.material.R.attr.colorOnSurfaceVariant
		)
		
		binding.set.text = if (position < 3)
			(position + 1).toString() + " 세트"
		else
			"연장전"
		
		//점수 표시
		binding.team1Point.text = play.pointResult[position][0].toString()
		binding.team2Point.text = play.pointResult[position][1].toString()
		
		//승리 팀에 따라 점수 칸 색 변경
		when (play.roundResult[position]) {
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
			listener.onTouchItem(position)
		}
	}
	
	/**
	 * ViewHolder의 개수인 [play]의 라운드 수를 반환한다.
	 *
	 * @return ViewHolder의 개수
	 */
	override fun getItemCount(): Int {
		return play.roundCount
	}
}