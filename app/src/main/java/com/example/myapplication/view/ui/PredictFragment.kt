package com.example.myapplication.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.myapplication.objects.PredictRank
import com.example.myapplication.R
import com.example.myapplication.databinding.PredictFragmentBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.view.model.MyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * 응원하는 팀이 특정 순위 내에 진입할 시나리오를 계산하는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 * @property[binding] PredictFragment의 ViewBinding 객체
 * @property[args] PredictFragment로 전달된 argument. 지정된 팀과 순위
 */
class PredictFragment: Fragment() {
    private val model: MyViewModel by activityViewModels()
    private lateinit var binding: PredictFragmentBinding
    private val args: PredictFragmentArgs by navArgs()

    /**
     * PredictFragment의 View를 생성
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
        binding = PredictFragmentBinding.inflate(inflater)

        return binding.root
    }

    /**
     * View가 생성되었을 때 호출
     *
     * 비동기적으로 특정 팀이 특정 순위 내에 드는 시나리오를 계산하는 작업을 수행하고,
     * 진행 바를 업데이트한다.
     * 작업 완료 시 [PredictResultFragment]로 전환된다.
     *
     * @param[view] 생성된 View 객체
     * @param[savedInstanceState] 이전 상태를 저장한 Bundle 객체
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.predictDescription.text = getString(R.string.check_chance, args.team, args.rank)

        //시나리오 계산
        viewLifecycleOwner.lifecycleScope.launch {
            val predictor = PredictRank(
                model.currentTeamList.value!!, model.currentPlayList.value!!,
                args.team, args.rank
            ) {
                CoroutineScope(Dispatchers.Main).launch { //진행 바 업데이트
                    binding.progressBar.progress = it
                }
            }
            val result = withContext(Dispatchers.Default) { predictor.predict() }

            val action = PredictFragmentDirections.actionPredictFragmentToPredictResultFragment(
                result,
                args.team,
                args.rank
            )
            findNavController().navigate(action)
        }
    }
}