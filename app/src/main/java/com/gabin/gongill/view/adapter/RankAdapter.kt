package com.gabin.gongill.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gabin.gongill.database.entity.Team
import com.gabin.gongill.databinding.RankItemBinding
import com.gabin.gongill.view.listener.OnTeamTouchListener

/**
 * 팀 순위를 보여주는 Adapter
 *
 * @param[tl] 팀 목록
 * @param[listener] 팀을 터치했을 때 호출되는 [OnTeamTouchListener]
 *
 * @property[teamList] 팀 목록
 */
class RankAdapter(tl: List<Team>?, val listener: OnTeamTouchListener) :
	RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	private var teamList: ArrayList<Team> = tl?.toCollection(ArrayList()) ?: arrayListOf()
	
	/**
	 * 팀 아이템의 ViewHolder
	 *
	 * @param[binding] RankItemBinding 객체
	 */
	class ViewHolder(val binding: RankItemBinding) : RecyclerView.ViewHolder(binding.root)
	
	/**
	 * ViewHolder가 생성될 때 호출
	 *
	 * @param[parent] 부모 ViewGroup 객체
	 * @param[viewType] View의 타입
	 *
	 * @return 생성된 ViewHolder
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(RankItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}
	
	/**
	 * ViewHolder의 개수인 [teamList]의 크기를 반환한다.
	 *
	 * @return ViewHolder의 개수
	 */
	override fun getItemCount(): Int {
		return teamList.size
	}
	
	/**
	 * ViewHolder가 연결될 때 호출
	 *
	 * 팀의 정보(순위, 별칭, 승리 수, 평균 포인트, 라운드 승점)을 표시하고,
	 * 클릭 이벤트의 listener를 추가한다.
	 *
	 * @param[holder] 연결할 ViewHolder 객체
	 * @param[position] ViewHolder의 위치
	 */
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val binding = (holder as ViewHolder).binding
		val team = teamList[position]
		
		//팀 정보 표시
		binding.rankRanking.text = team.rank.toString() //순위
		binding.rankTeamName.text = team.alias //팀 별칭
		binding.rankWin.text = team.win.toString() //승리 수
		val point = team.point.toFloat() / team.roundCount.toFloat()
		binding.rankPoint.text = if (point.isNaN()) "0" else String.format("%.1f", point) //평균 포인트
		binding.rankRndPt.text = team.roundWin.toString() //라운드 승점
		
		//팀 클릭 시
		binding.rankTeamInfo.setOnClickListener {
			listener.onTouchItem(team)
		}
	}
	
	/**
	 * 연결된 데이터를 변경한다.
	 *
	 * @param[list] 변경할 데이터
	 */
	fun changeData(list: List<Team>) {
		teamList = list.toCollection(ArrayList())
		notifyDataSetChanged()
	}
}