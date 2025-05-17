package com.example.myapplication.view.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Team
import com.example.myapplication.databinding.PredictDialogBinding
import com.example.myapplication.databinding.RankFragmentBinding
import com.example.myapplication.view.adapter.RankAdapter
import com.example.myapplication.view.listener.OnTeamTouchListener
import com.example.myapplication.view.model.MyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 팀 순위를 보여주는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 ViewModel 객체
 * @property[binding] RankFragment의 ViewBinding 객체
 */
class RankFragment : Fragment(), OnTeamTouchListener {
	private val model: MyViewModel by activityViewModels()
	private lateinit var binding: RankFragmentBinding
	
	/**
	 * RankFragment의 View를 생성
	 *
	 * @param[inflater] View 생성을 위한 LayoutInflater 객체
	 * @param[container] 생성된 View의 부모 ViewGroup 객체
	 * @param[savedInstanceState] 이전 상태 정보를 저장한 Bundle 객체
	 *
	 * @return 생성된 View 객체
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = RankFragmentBinding.inflate(inflater)
		
		return binding.root
	}
	
	/**
	 * View가 생성되었을 때 호출
	 *
	 * adapter를 연결하여 순위대로 팀을 표시하고,
	 * [MyViewModel.currentTeamList]을 ovserve하여 UI에 반영한다.
	 * 플로팅 버튼 터치 이벤트의 listener를 설정한다.
	 *
	 * @param[view] Fragment의 View 객체
	 * @param[savedInstanceState] 이전 상태가 저장된 Bundle 객체
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		//순위에 따라 팀 목록 표시
		binding.rankView.layoutManager =
			LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
		val adapter = RankAdapter(model.currentTeamList.value, this)
		binding.rankView.adapter = adapter
		
		//현재 팀 리스트가 변경될 때마다 adapter의 데이터를 업데이트
		model.currentTeamList.observe(viewLifecycleOwner) {
			adapter.changeData(it.sortedBy { play -> play.rank })
		}
		
		//남은 경기 수가 범위 밖일 경우 Diolog 표시
		val maxRemain = 12
		binding.rankItemHPredictBtn.setOnClickListener {
			val remain = model.currentPlayList.value!!.count { it.winIdx == null }
			if (remain >= maxRemain)
				model.toastObserver.value = "${maxRemain}개 이상의 경기가 남은 경우 시나리오를 확인 할 수 없습니다."
			else
				showPredictDialog()
		}
	}
	
	/**
	 * 시나리오를 확인하기 위한 Dialog 표시한다.
	 * 응원 팀과 목표 순위를 지정해 [PredictFragment]로 전환한다.
	 */
	private fun showPredictDialog() {
		val dialogBinding = PredictDialogBinding.inflate(layoutInflater)
		
		val arrayAdapter = ArrayAdapter(
			requireContext(),
			R.layout.group_array_item, model.currentTeamList.value!!.map { it.alias })
		dialogBinding.predictDialogTeamText.setAdapter(arrayAdapter)
		
		//목표 순위 입력 시 범위 밖의 순위일 경우 에러 메시지
		dialogBinding.predictDialogRankText.doOnTextChanged { text, _, _, _ ->
			dialogBinding.predictDialogRank.error = null
			if ((text.toString().toIntOrNull() ?: Int.MAX_VALUE) > model.currentTeamList.value!!.size)
				dialogBinding.predictDialogRank.error = getString(R.string.error_rank_max)
		}
		
		//Dialog 생성
		val builder = MaterialAlertDialogBuilder(requireContext())
			.setTitle(" ")
			.setNegativeButton("취소", null)
			.setPositiveButton("시나리오 확인하기", null)
			.setView(dialogBinding.root)
			.show()
		
		//확인 버튼을 누를 시
		builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
			var isError = dialogBinding.predictDialogRank.error != null
			if (dialogBinding.predictDialogTeamText.text.isNullOrBlank()) { //팀 미선택 에러
				dialogBinding.predictDialogTeam.error = getString(R.string.error_team_required)
				isError = true
			}
			if (dialogBinding.predictDialogRankText.text.isNullOrBlank()) { //순위 미입력 에러
				dialogBinding.predictDialogRank.error = getString(R.string.error_rank_required)
				isError = true
			}
			
			if (!isError) { //문제가 없으면 PredictFragment로 이동하여 시나리오 확인
				val team = dialogBinding.predictDialogTeamText.text.toString()
				val rank = dialogBinding.predictDialogRankText.text.toString().toInt()
				
				val action = RankFragmentDirections.actionRankFragmentToPredictFragment(team, rank)
				Navigation.findNavController(requireActivity(), R.id.hostFragment)
					.navigate(action)
				builder.dismiss()
			}
		}
	}
	
	/**
	 * 팀 아이템을 선택 시 호출
	 *
	 * 터치한 팀의 상세 정보를 보여주는 [TeamFragment]로 이동한다.
	 *
	 * @param[team] 터치한 팀의 [Team] 객체
	 */
	override fun onTouchItem(team: Team) {
		model.selectedTeam = team
		Navigation.findNavController(requireActivity(), R.id.hostFragment)
			.navigate(R.id.action_rankFragment_to_teamFragment)
	}
}