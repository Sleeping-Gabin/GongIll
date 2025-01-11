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

        binding.predictDescription.text = getString(R.string.check_chance, args.team, args.rank)

        CoroutineScope(Dispatchers.Main).launch {
            val predictor = PredictRank(model.currentTeamList.value!!, model.currentPlayList.value!!,
                args.team, args.rank) { CoroutineScope(Dispatchers.Main).launch { binding.progressBar.progress = it } }
            val result = CoroutineScope(Dispatchers.Default).async { predictor.predict() }.await()

            val action = PredictFragmentDirections.actionPredictFragmentToPredictResultFragment(result, args.team, args.rank)
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(action)
        }


        return binding.root
    }

}