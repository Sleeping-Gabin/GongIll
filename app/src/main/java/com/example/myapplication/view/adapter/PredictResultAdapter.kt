package com.example.myapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.PredictResultItemBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.R

/**
 * 응원하는 팀이 목표 순위 내에 드는 시나리오를 보여주는 Adapter
 *
 * @param[result] 응원하는 팀이 목표 순위 내에 드는 시나리오인 [PredictResult] 객체
 *
 * @property[teams] 팀 목록
 * @property[winScenario] 성공 시나리오 목록
 * @property[roundScenario] 라운드 비교가 필요한 시나리오 목록
 * @property[resultList] 화면에 표시할 시나리오 목록
 */
class PredictResultAdapter(result: PredictResult): RecyclerView.Adapter<PredictResultAdapter.ViewHolder>() {
    private val teams = result.teams
    private var winScenario = result.winScenario
    private var roundScenario = result.roundScenario

    private var resultList = result.winScenario

    /**
     * 화면에 표시할 시나리오의 모드를 나타내는 enum 클래스
     */
    enum class Mode { WIN, ROUND }

    /**
     * 시나리오 아이템의 ViewHolder
     *
     * @param[binding] PredictResultItemBinding 객체
     * @param[context] 부모의 Context 객체
     */
    class ViewHolder(val binding: PredictResultItemBinding, val context: Context): RecyclerView.ViewHolder(binding.root)

    /**
     * ViewHolder가 생성될 때 호출
     *
     * @param[parent] 부모 ViewGroup 객체
     * @param[viewType] View의 타입
     *
     * @return 생성된 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PredictResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), parent.context)
    }

    /**
     * ViewHolder가 연결될 때 호출
     *
     * 시나리오의 각 경기 결과를 표시한다.
     *
     * @param[holder] 연결할 ViewHolder 객체
     * @param[position] ViewHolder의 위치
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.predictItemView.text = ""
        val binding = holder.binding
        val scenario = resultList[position]

        //시나리오의 각 경기 결과를 표시
        for (result in scenario.teamResults) {
            val team1 = teams[result.team1Idx]
            val team2 = teams[result.team2Idx]
            val winTeam = teams[result.winner!!]

            val str = holder.context.getString(R.string.predictResult, team1, team2, winTeam)
            if (binding.predictItemView.text.isNotEmpty())
                binding.predictItemView.append("\n")
            binding.predictItemView.append(str)
        }
    }

    /**
     * 모드를 변경하고 그에 맞춰 [resultList]를 변경한다.
     *
     * @param[newMode] 변경할 [Mode]
     */
    fun changeMode(newMode: Mode) {
        resultList = when (newMode) {
            Mode.WIN -> {
                winScenario
            }

            Mode.ROUND -> {
                roundScenario
            }
        }
        notifyDataSetChanged()
    }

    /**
     * [resultList]를 비운다.
     */
    fun clear() {
        resultList = listOf()
    }

    /**
     * ViewHolder의 개수인 [resultList]의 크기를 반환한다.
     *
     * @return ViewHolder의 개수
     */
    override fun getItemCount(): Int {
        //println(resultList.size)
        return resultList.size
    }
}