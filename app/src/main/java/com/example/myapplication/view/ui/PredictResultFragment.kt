package com.example.myapplication.view.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.example.myapplication.R
import com.example.myapplication.databinding.PredictResultFragmentBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.view.adapter.PredictResultAdapter
import com.google.android.material.color.MaterialColors

/**
 * 특정 팀이 특정 순위 내에 들 수 있는 시나리오를 보여주는 Fragment
 *
 * @property[args] Fragment로 전달된 argument.
 * 계산 결과인 predictResult, 응원 팀인 team, 목표 순위인 rank.
 * @property[binding] PredictResultFragment의 ViewBinding 객체
 * @property[result] 시나리오 계산 결과인 [PredictResult] 객체
 * @property[mode] 화면에 표시할 결과의 모드의 [PredictResultAdapter.Mode] 객체
 * @property[adapter] [PredictResultAdapter] 객체
 */
class PredictResultFragment : Fragment() {
	private val args: PredictResultFragmentArgs by navArgs()
	private lateinit var binding: PredictResultFragmentBinding
	
	private lateinit var result: PredictResult
	private var mode = PredictResultAdapter.Mode.WIN
	private lateinit var adapter: PredictResultAdapter
	
	/**
	 * PredictResultFragment의 View를 생성
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
		binding = PredictResultFragmentBinding.inflate(inflater)
		
		result = args.predictResult
		
		return binding.root
	}
	
	/**
	 * View가 생성되었을 때 호출
	 *
	 * 모드에 따라 시나리오 목록을 표시한다.
	 *
	 * - 뒤로 가기 버튼 동작 설정
	 * - adapter를 연결하여 시나리오 목록을 표시
	 * - 모드 버튼 체크 이벤트의 listener를 설정
	 * - 모드에 따른 텍스트 표시
	 *
	 * @param[view] 생성된 View 객체
	 * @param[savedInstanceState] 이전 상태를 저장한 Bundle 객체
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		//뒤로 가기로 "순위" 탭으로 이동
		requireActivity().onBackPressedDispatcher.addCallback(this) {
			Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
		}
		
		//시나리오 목록 표시
		binding.predictResultView.layoutManager =
			LinearLayoutManager(requireContext(), VERTICAL, false)
		adapter = PredictResultAdapter(result)
		binding.predictResultView.adapter = adapter
		
		//체크한 버튼에 따라 모드를 바꾸고 해당 시나리오 목록 표시
		binding.predictResultBtns.addOnButtonCheckedListener { _, checkedId, isChecked ->
			when (checkedId) {
				R.id.predictResult_win -> { //확정 성공 or 실패
					if (isChecked) {
						mode = PredictResultAdapter.Mode.WIN
						adapter.changeMode(mode)
						setPredictText()
					}
				}
				
				R.id.predictResult_round -> { //라운드 비교
					if (isChecked) {
						mode = PredictResultAdapter.Mode.ROUND
						adapter.changeMode(mode)
						setPredictText()
					}
				}
			}
			
		}
		
		setPredictText()
	}
	
	/**
	 * 시나리오 계산 결과에 따라 화면에 표시할 텍스트를 설정
	 */
	private fun setPredictText() {
		if (result.reverse)  //실패 & 라운드 비교
			setPredictTextReverse()
		else //성공 & 라운드 비교
			setPredictTextNormal()
	}
	
	/**
	 * [result]의 [PredictResult.reverse]가 true일 경우의 텍스트 표시
	 *
	 * 실패 & 라운드 비교 시나리오의 존재 여부에 따라 화면에 표시할 텍스트를 설정한다.
	 */
	private fun setPredictTextReverse() {
		binding.predictResultWin.text = "확정 실패"
		
		if (result.winScenario.isEmpty() && result.roundScenario.isEmpty()) { //확정 성공
			binding.predictResultTxt.text =
				getString(R.string.predict_perfect_win, args.team, args.rank)
			
			binding.predictResultWin.visibility = View.GONE
			binding.predictResultRound.visibility = View.GONE
		} else if (result.winScenario.isEmpty()) { //실패 상황 없음
			binding.predictResultTxt.text =
				getString(
					R.string.predict_reverse_rnd,
					result.roundScenario.size,
					args.team,
					args.rank
				)
			
			binding.predictResultWin.visibility = View.GONE
			binding.predictResultBtns.check(R.id.predictResult_round)
		} else if (result.roundScenario.isEmpty()) { //라운드 비교 상황 없음
			val text = getString(
				R.string.predict_reverse_win,
				result.winScenario.size,
				args.team,
				args.rank
			)
			val color =
				MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
			val spannable = SpannableString(text)
			val length = result.winScenario.size.toString().length
			spannable.setSpan(ForegroundColorSpan(color), 10 + length, 15 + length, 0)
			binding.predictResultTxt.text = spannable
			
			binding.predictResultRound.visibility = View.GONE
		} else { //실패 상황, 라운드 비교 상황 모두 존재
			when (mode) {
				PredictResultAdapter.Mode.WIN -> { //실패 시나리오 표시
					val text = getString(
						R.string.predict_reverse_check,
						result.winScenario.size,
						args.team,
						args.rank
					)
					val color = MaterialColors.getColor(
						binding.root,
						com.google.android.material.R.attr.colorError
					)
					val spannable = SpannableString(text)
					spannable.setSpan(
						ForegroundColorSpan(color),
						text.length - 29,
						text.length - 23,
						0
					)
					binding.predictResultTxt.text = spannable
				}
				
				PredictResultAdapter.Mode.ROUND -> { //라운드 비교 시나리오 표시
					binding.predictResultTxt.text =
						getString(
							R.string.predict_check_rnd,
							result.roundScenario.size,
							args.team,
							args.rank
						)
				}
			}
			
		}
	}
	
	/**
	 * [result]의 [PredictResult.reverse]가 false일 경우의 텍스트 표시
	 *
	 * 성공 & 라운드 비교 시나리오의 존재 여부에 따라 화면에 표시할 텍스트를 설정한다.
	 */
	private fun setPredictTextNormal() {
		binding.predictResultWin.text = "확정 성공"
		
		if (result.winScenario.isEmpty() && result.roundScenario.isEmpty()) { //확정 실패
			val text = getString(R.string.predict_null, args.team, args.rank)
			val color =
				MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
			val spannable = SpannableString(text)
			spannable.setSpan(ForegroundColorSpan(color), text.length - 5, text.length, 0)
			binding.predictResultTxt.text = spannable
			
			binding.predictResultWin.visibility = View.GONE
			binding.predictResultRound.visibility = View.GONE
		} else if (result.roundScenario.size == 1 && result.roundScenario[0].teamResults.isEmpty()) { //모든 상황 라운드 비교 필요
			binding.predictResultTxt.text = getString(R.string.predict_perfect_rnd, args.team, args.rank)
			
			binding.predictResultWin.visibility = View.GONE
			
			adapter.clear()
		} else if (result.winScenario.isEmpty()) { //성공 상황 없음
			val text =
				getString(R.string.predict_rnd, result.roundScenario.size, args.team, args.rank)
			val color =
				MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
			val spannable = SpannableString(text)
			spannable.setSpan(ForegroundColorSpan(color), text.length - 7, text.length - 4, 0)
			binding.predictResultTxt.text = spannable
			
			binding.predictResultWin.visibility = View.GONE
			binding.predictResultBtns.check(R.id.predictResult_round)
		} else if (result.roundScenario.isEmpty()) { //라운드 비교 상황 없음
			binding.predictResultTxt.text =
				getString(R.string.predict_win, result.winScenario.size, args.team, args.rank)
			
			binding.predictResultRound.visibility = View.GONE
		} else { //성공 상황, 라운드 비교 상황 모두 존재
			when (mode) {
				PredictResultAdapter.Mode.WIN -> { //성공 시나리오 표시
					binding.predictResultTxt.text =
						getString(
							R.string.predict_check,
							result.winScenario.size,
							args.team,
							args.rank
						)
				}
				
				PredictResultAdapter.Mode.ROUND -> { //라운드 비교 시나리오 표시
					binding.predictResultTxt.text =
						getString(
							R.string.predict_check_rnd,
							result.roundScenario.size,
							args.team,
							args.rank
						)
				}
			}
		}
	}
}