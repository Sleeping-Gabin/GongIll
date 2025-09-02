package com.gabin.gongill.view.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gabin.gongill.R
import com.gabin.gongill.database.entity.Team
import com.gabin.gongill.databinding.AddTeamDialogBinding
import com.gabin.gongill.databinding.TeamFragmentBinding
import com.gabin.gongill.view.adapter.TeamPlayAdapter
import com.gabin.gongill.view.model.MyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min

/**
 * 팀의 정보를 보여주는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 * @property[binding] TeamFragment의 ViewBinding 객체
 * @property[team] 정보를 보여줄 [Team] 객체
 */
class TeamFragment : Fragment() {
	private val model: MyViewModel by activityViewModels()
	private lateinit var binding: TeamFragmentBinding
	private lateinit var team: Team
	
	/**
	 * TeamFragment의 View를 생성
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
		binding = TeamFragmentBinding.inflate(inflater)
		
		team = model.selectedTeam!!
		
		return binding.root
	}
	
	/**
	 * View가 생성되었을 떼 호출
	 *
	 * 앱바를 초기화하고 팀의 정보를 표시한다.
	 *
	 * - 뒤로 가기 동작 설정
	 * - adapter를 연결하여 팀의 경기 정보 표시
	 * - 팀 정보(순위, 승/패, 평균 포인트, 무승부) 표시
	 * - Navigation Controller와 앱바 설정
	 * - 앱바 메뉴 아이템 터치 이벤트 listener 추가
	 *
	 * @param[view] 생성된 View 객체
	 * @param[savedInstanceState] 이전 상태 정보를 저장한 Bundle 객체
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		//뒤로 가기 시 이전 탭으로 이동
		requireActivity().onBackPressedDispatcher.addCallback(this) {
			Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
		}
		
		//팀의 경기 정보 표시
		binding.teamPlayInfoView.layoutManager =
			LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
		binding.teamPlayInfoView.adapter = TeamPlayAdapter(team.alias, model.getTeamPlay())
		
		//팀 정보 표시
		binding.teamRank.text = team.rank.toString()
		binding.teamWinLose.text = String.format("%d / %d", team.win, team.lose)
		binding.teamRoundPt.text = team.roundWin.toString()
		val point = team.point.toFloat() / team.roundCount.toFloat()
		binding.teamPt.text = if (point.isNaN()) "0" else String.format("%.1f", point)
		binding.teamDrawPt.text = team.drawRound.toString()
		
		//앱바 설정
		val navController = findNavController()
		val appBarConfiguration = AppBarConfiguration(navController.graph)
		binding.toolBar.setupWithNavController(navController, appBarConfiguration)
		binding.toolBar.navigationIcon =
			AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_back_white)
		binding.toolBar.title = model.selectedTeam?.name
		
		binding.toolBar.setOnMenuItemClickListener {
			when (it.itemId) {
				R.id.editName -> { //팀 이름 변경
					editTeamName()
					true
				}
				
				R.id.delete -> { //팀 삭제
					deleteTeam()
					true
				}
				
				else -> false
			}
		}
	}
	
	/**
	 * 팀의 이름을 수정하는 Dialog를 생성하고,
	 * 입력한 정보로 팀의 이름과 별칭 수정
	 */
	private fun editTeamName() {
		val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)
		dialogBinding.addTeamDialogGroupName.visibility = View.GONE
		
		//현재 팀 이름을 기본 값으로
		dialogBinding.addTeamDialogTeamNameText.setText(team.name)
		dialogBinding.addTeamDialogAliasText.setText(team.alias)
		
		//팀 이름을 입력시
		dialogBinding.addTeamDialogTeamNameText.addTextChangedListener(object : TextWatcher {
			var isSame = false
			
			//팀 이름과 별칭이 같은지 확인
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
				dialogBinding.addTeamDialogTeamName.error = null
				val name = s?.filterNot { it.isWhitespace() }
				isSame = name?.subSequence(0, min(name.length, 4)).toString() ==
								dialogBinding.addTeamDialogAliasText.text.toString()
			}
			
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
			}
			
			//별칭이 팀 이름과 같을 시 변경한 후에도 같게 적용
			override fun afterTextChanged(s: Editable?) {
				if (isSame && s != null && s.filterNot { it.isWhitespace() }.length
					<= dialogBinding.addTeamDialogAlias.counterMaxLength
				) {
					dialogBinding.addTeamDialogAliasText.setText(s.filterNot { it.isWhitespace() })
				}
			}
		})
		
		//별칭이 너무 길 경우 에러 메시지
		dialogBinding.addTeamDialogAliasText.doOnTextChanged { text, _, _, _ ->
			if (text != null && text.length > dialogBinding.addTeamDialogAlias.counterMaxLength)
				dialogBinding.addTeamDialogAlias.error = getString(
					R.string.error_alias_maxCount,
					dialogBinding.addTeamDialogAlias.counterMaxLength
				)
			else
				dialogBinding.addTeamDialogAlias.error = null
		}
		
		//Dialog 생성
		val builder = MaterialAlertDialogBuilder(requireContext())
			.setTitle(" ")
			.setNegativeButton("취소", null)
			.setPositiveButton("확인", null)
			.setView(dialogBinding.root)
			.show()
		
		//확인 버튼을 누를 시
		builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
			var isError = dialogBinding.addTeamDialogAlias.error != null
			if (dialogBinding.addTeamDialogTeamNameText.text.isNullOrBlank()) { //팀명 미입력시 에러
				dialogBinding.addTeamDialogTeamName.error = getString(R.string.error_teamName_required)
				isError = true
			}
			
			if (!isError) { //문제가 없으면 팀 이름과 별칭 변경
				val teamName = dialogBinding.addTeamDialogTeamNameText.text.toString()
				val teamAlias = dialogBinding.addTeamDialogAliasText.text.toString()
				model.changeTeamName(team, teamName, teamAlias)
				binding.toolBar.title = teamName
				builder.dismiss()
			}
		}
	}
	
	/**
	 * [team]을 삭제하는 Dialog를 생성하고 삭제
	 */
	private fun deleteTeam() {
		//dialog 생성
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("${team.groupName}의 팀 '${team.name}'을(를) 삭제합니다.")
			.setMessage("삭제 하면 되돌릴 수 없습니다. 해당 팀에 포함된 경기 데이터도 함께 삭제됩니다.")
			.setPositiveButton("삭제") { _, _ ->
				model.deleteTeam(team)
				Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
			}
			.setNegativeButton("취소", null)
			.show()
	}
	
}