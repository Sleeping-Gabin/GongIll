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

/**
 * 그룹별 팀 목록을 보여주는 Adapter
 *
 * @param[teamList] 팀 목록
 * @param[listener] 팀을 터치했을 때 호출되는 [OnGroupTeamTouchListener]
 * @param[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 *
 * @property[teamList] 팀 목록
 */
class GroupTeamAdapter(teamList: List<Team>?, val listener: OnGroupTeamTouchListener, val model: MyViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var teamList: List<Team> = teamList?.toList() ?: listOf()

    /**
     * 그룹 상세 아이템의 ViewHolder
     *
     * @param[binding] GroupDetailItemBinding 객체
     * @param[context] 부모의 Context 객체
     */
    class ViewHolder(val binding: GroupDetailItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root)

    /**
     * ViewHolder가 생성될 때 호출
     *
     * @param[parent] 부모 ViewGroup 객체
     * @param[viewType] View의 타입
     *
     * @return 생성된 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(GroupDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
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
     * 팀 정보(이름, 경기 진행 상황)을 표시하고 팀 클릭 이벤트의 listener를 추가한다.
     *
     * @param[holder] 연결할 ViewHolder 객체
     * @param[position] ViewHolder의 위치
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        val team = teamList[position]

        val teamPlay = model.getTeamPlay(team)
        val finishPlaySize = teamPlay.count { p -> p.winTeam != null }

        //팀 정보 표시
        binding.groupDetailItemTeamName.text = team.alias
        binding.groupDetailItemPlayInfo.text = holder.context.getString(R.string.groupPlayInfo, teamPlay.size, finishPlaySize)
        binding.root.setOnClickListener {
            listener.onTouchItem(team)
        }
    }

    /**
     * 연결된 데이터를 변경한다.
     *
     * @param[newList] 변경할 데이터
     */
    fun changeData(newList: List<Team>?) {
        teamList = newList?.toList() ?: listOf()
        notifyDataSetChanged()
    }
}