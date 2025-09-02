package com.gabin.gongill.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.gabin.gongill.database.entity.Play
import com.gabin.gongill.databinding.TeamItemBinding

/**
 * 팀별 경기 일정을 보여주는 Adapter
 *
 * @param[team] 팀 이름
 * @param[playList] [team]의 경기 일정
 *
 * @property[playList] [team]의 경기 일정
 */
class TeamPlayAdapter(val team: String, playList: List<Play>?) :
	RecyclerView.Adapter<TeamPlayAdapter.ViewHolder>() {
	private var playList: ArrayList<Play> =
		playList?.sortedBy { it.order }?.toCollection(ArrayList()) ?: arrayListOf()
	
	/**
	 * 경기 일정 아이템의 ViewHolder
	 *
	 * @param[binding] TeamItemBinding 객체
	 * @param[context] ViewHolder의 Context
	 */
	class ViewHolder(val binding: TeamItemBinding, val context: Context) :
		RecyclerView.ViewHolder(binding.root)
	
	/**
	 * ViewHolder가 생성될 때 호출
	 *
	 * @param[parent] 부모 ViewGroup 객체
	 * @param[viewType] view의 타입
	 *
	 * @return 생성된 VieweHolder
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			TeamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
			parent.context
		)
	}
	
	/**
	 * ViewHolder의 개수인 [playList]의 크기를 반환한다.
	 *
	 * @return ViewHolder의 개수
	 */
	override fun getItemCount(): Int {
		return playList.size
	}
	
	/**
	 * ViewHolder가 연결될 때 호출
	 *
	 * 상대 팀과의 경기 점수를 표시한다.
	 *
	 * @param[holder] 연결할 ViewHolder 객체
	 * @param[position] ViewHolder의 위치
	 */
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val binding = holder.binding
		val play = playList[position]
		val teamIdx = if (play.team1 == team) 0 else 1
		
		//상대 팀 이름
		binding.teamItemTeamName.text = if (play.team1 == team) play.team2 else play.team1
		
		//점수 표시, 우리 팀 : 상대 팀
		for (rndTxt in binding.teamItemPlayInfo.children.withIndex()) {
			(rndTxt.value as TextView).text = if (play.roundResult[rndTxt.index] != null)
				String.format(
					"%2d : %-2d",
					play.pointResult[rndTxt.index][teamIdx],
					play.pointResult[rndTxt.index][1 - teamIdx]
				)
			else
				"-"
		}
		
		//사이클 수가 2 이상이면 동일팀 간의 몇번째 경기인지 표시
		if (play.playNum > 0) {
			binding.teamItemPlayNum.visibility = View.VISIBLE
			binding.teamItemPlayNum.text = play.playNum.toString()
		}
	}
}