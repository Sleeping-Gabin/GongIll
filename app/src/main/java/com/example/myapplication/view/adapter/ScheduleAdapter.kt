package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.entity.Play
import com.example.myapplication.databinding.ScheduleItemBinding
import com.example.myapplication.view.adapter.ScheduleAdapter.Filter
import com.example.myapplication.view.listener.OnPlayDragListener
import java.util.Collections

/**
 * 경기 일정을 보여주는 Adapter
 *
 * @param[list] 경기 일정
 * @param[listener] 경기를 터치했을 때 호출되는 [OnPlayDragListener]
 *
 * @property[playList] 경기 일정
 * @property[filterList] 필터링된 경기 일정
 * @property[filter] 경기 일정을 필터링할 [Filter] 객체
 */
class ScheduleAdapter(list: List<Play>?, val listener: OnPlayDragListener) :
	RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {
	private var playList: ArrayList<Play> = list?.toCollection(ArrayList()) ?: arrayListOf()
	private var filterList = ArrayList(playList)
	var filter = Filter.ALL
	
	/**
	 * 경기 일정을 필터링하는 방법을 나타내는 enum 클래스
	 */
	enum class Filter { ALL, FINISH, YET }
	
	/**
	 * 경기 일정 아이템의 ViewHolder
	 *
	 * 스와이프가 안 된 아이템을 터치할 시의 listener를 추가한다.
	 *
	 * @param[binding] ScheduleItemBinding 객체
	 *
	 * @property[isSwiped] 스와이프 되었는지 여부
	 */
	class ViewHolder(val binding: ScheduleItemBinding) : RecyclerView.ViewHolder(binding.root) {
		var isSwiped = false
		
		init {
			binding.scheduleFrame.setOnClickListener {
				if (!isSwiped) { //스와이프가 안 되었을 때만 점수 표시/비표시
					if (binding.team1Score.visibility == View.VISIBLE) { //점수 비표시
						binding.team1Score.visibility = View.GONE
						binding.drawScore.visibility = View.GONE
						binding.team2Score.visibility = View.GONE
					} else { //점수 표시
						binding.team1Score.visibility = View.VISIBLE
						binding.drawScore.visibility = View.VISIBLE
						binding.team2Score.visibility = View.VISIBLE
					}
				}
				
			}
		}
	}
	
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
			ScheduleItemBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}
	
	/**
	 * ViewHolder의 개수인 [filterList]의 크기를 반환한다.
	 *
	 * @return ViewHolder의 개수
	 */
	override fun getItemCount(): Int {
		return filterList.size
	}
	
	/**
	 * ViewHolder가 연결될 때 호출
	 *
	 * 팀 이름과 동일 팀 간의 경기 휫수를 표시하고, 경기 점수를 보여준다.
	 *
	 * @param[holder] 연결할 ViewHolder 객체
	 * @param[position] ViewHolder의 위치
	 */
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val binding = holder.binding
		val play = filterList[position]
		
		//팀 이름
		binding.team1.text = play.team1
		binding.team2.text = play.team2
		
		//경기 점수
		binding.team1Score.text = play.roundResult.count { result -> result == 0 }.toString()
		binding.team1Score.visibility = View.GONE
		binding.team2Score.text = play.roundResult.count { result -> result == 1 }.toString()
		binding.team2Score.visibility = View.GONE
		binding.drawScore.text = play.roundResult.count { result -> result == 2 }.toString()
		binding.drawScore.visibility = View.GONE
		
		//동일 팀 간의 몇번째 경기인지 표시
		if (play.playNum > 0) {
			binding.playNum.text = play.playNum.toString()
			binding.playNum.visibility = View.VISIBLE
		} else
			binding.playNum.visibility = View.GONE
		
		binding.scheduleSwipe.setOnClickListener {
			listener.onTouchItem(play)
		}
	}
	
	/**
	 * 경기 아이템을 드래그해 움직일 때 호출
	 *
	 * 두 이이템의 순서를 바꾼다.
	 *
	 * @param[from] 변경할 아이템의 인덱스
	 * @param[to] 변경하는 위치의 아이템의 인덱스
	 */
	fun dragItem(from: Int, to: Int) {
		//순서 데이터 변경
		val fromPlay = filterList[from]
		val toPlay = filterList[to]
		changeItemOrder(fromPlay, toPlay)
		
		//경기 목록에서의 순서 변경
		val fromIdx = playList.indexOf(fromPlay)
		val toIdx = playList.indexOf(toPlay)
		Collections.swap(playList, fromIdx, toIdx)
		
		//필터링 목록에서의 순서 변경
		Collections.swap(filterList, from, to)
		notifyItemMoved(from, to)
		listener.onDragItem(fromPlay, toPlay)
	}
	
	/**
	 * 아이템의 [Play] 객체에서 순서인 [Play.order]를 변경한다.
	 *
	 * @param[fromPlay] 변경할 아이템
	 * @param[toPlay] 변경하는 위치의 아이템
	 */
	private fun changeItemOrder(fromPlay: Play, toPlay: Play) {
		val temp = fromPlay.order
		fromPlay.order = toPlay.order
		toPlay.order = temp
	}
	
	/**
	 * 연결된 데이터를 변경한다.
	 *
	 * @param[newPlayList] 변경할 데이터
	 */
	fun changeData(newPlayList: List<Play>) {
		playList = newPlayList.toCollection(kotlin.collections.ArrayList())
		filterData()
	}
	
	/**
	 * [filter]에 따라 [filterList]를 필터링한다.
	 */
	fun filterData() {
		filterList = when (filter) {
			Filter.ALL -> ArrayList(playList)
			Filter.YET -> playList.filter { p -> p.winTeam == null }.toCollection(ArrayList())
			Filter.FINISH -> playList.filter { p -> p.winTeam != null }.toCollection(ArrayList())
		}
		filterList.sortBy { it.order }
		notifyDataSetChanged()
	}
}