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

/**
 * 그룹 목록을 보여주는 Adapter
 *
 * @param[category] 그룹의 카테고리 이름
 * @param[groupList] [category]에 속한 그룹 목록
 * @param[listener] 아이템을 터치했을 때 호출되는 [OnGroupTouchListener]
 * @param[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 *
 * @property[groupList] [category]에 속한 그룹 목록
 */
class GroupAdapter(val category: String, groupList: List<Group>?, val listener: OnGroupTouchListener, val model: MyViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var groupList = groupList ?: listOf()

    /**
     * 그룹 아이템의 ViewHolder
     *
     * @param[binding] GroupItemBinding 객체
     * @param[context] 부모의 Context 객체
     */
    class ViewHolder(val binding: GroupItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root)

    /**
     * 그룹을 추가하는 헤더 아이템의 ViewHolder
     *
     * @param[binding] GroupItemHeaderBinding 객체
     */
    class HeaderViewHolder(val binding: GroupItemHeaderBinding): RecyclerView.ViewHolder(binding.root)

    /**
     * ViewHolder가 생성될 때 호출
     *
     * [viewType]이 0이면 [ViewHolder]를, 1이면 [HeaderViewHolder]를 생성한다.
     *
     * @param[parent] 부모 ViewGroup 객체
     * @param[viewType] view의 타입. 0이면 그룹 아이템, 1이면 헤더 아이템
     *
     * @return 생성된 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0)
            ViewHolder(GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
        else //header
            HeaderViewHolder(GroupItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * ViewHolder가 연결될 때 호출
     *
     * 일반 아이템에 경기 진행 상황을 표시하고 listener를 연결한다.
     * 헤더 아이템에 그룹 추가 버튼 클릭 이벤트의 listener를 연결한다.
     *
     * @param[holder] 연결할 ViewHolder 객체
     * @param[position] ViewHolder의 위치
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> { //일반
                val binding = (holder as ViewHolder).binding
                val group = groupList[position-1]

                binding.groupItemTeam.text = group.name

                //경기 진행 상황 표시
                val allPlay = model.getGroupPlay(group)
                val finishPlaySize = allPlay.count { p -> p.winTeam != null }
                binding.groupItemDescription.text = holder.context.getString(R.string.groupPlayInfo, allPlay.size, finishPlaySize)

                binding.root.setOnClickListener { //그룹 상세 확인
                    listener.onTouchItem(group)
                }

                binding.groupItemAddTeamButton.setOnClickListener { //팀 추가
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

    /**
     * ViewHolder의 개수인 [groupList] 크기 + 1(헤더)을 반환한다.
     *
     * @return ViewHolder의 개수
     */
    override fun getItemCount(): Int {
        return groupList.size + 1
    }

    /**
     * ViewHolder의 viewTyoe을 반환한다.
     *
     * @param[position] ViewHolder의 위치
     *
     * @return 첫번째 아이템이면 1, 아니면 0
     */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 1 else 0
    }

    /**
     * 연결된 데이터를 변경한다.
     *
     * @param[list] 변경할 데이터
     */
    fun changeData(list: List<Group>) {
        groupList = list
        notifyDataSetChanged()
    }
}