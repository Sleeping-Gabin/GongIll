package com.example.myapplication.view.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Play
import com.example.myapplication.databinding.EditDialogBinding
import com.example.myapplication.databinding.PlayFragmentBinding
import com.example.myapplication.view.adapter.PlayAdapter
import com.example.myapplication.view.listener.OnPointTouchListener
import com.example.myapplication.view.model.MyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Integer.min

/**
 * 경기 결과를 표시하는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 * @property[binding] PlayFragment의 View Binding 객체
 * @property[play] 표시할 경기의 [Play] 객체
 */
class PlayFragment: Fragment(), OnPointTouchListener {
    private val model: MyViewModel by activityViewModels()
    lateinit var binding: PlayFragmentBinding
    lateinit var play: Play

    /**
     * PlayFragment의 View를 생성
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
        binding = PlayFragmentBinding.inflate(inflater)

        return binding.root
    }

    /**
     * Fragment의 View가 생성된 후 호출
     *
     * 뒤로가기 버튼의 동작을 설정하고 툴바를 초기화한다.
     * adapter를 연결해 경기 정보를 표시한다.
     *
     * @param[view] 생성된 View 객체
     * @param[savedInstanceState] 이전 상태를 저장한 Bundle 객체
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //뒤로 가기 시 이전 탭("경기")으로 이동
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }

        //툴 바 초기화
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.playToolBar.setupWithNavController(navController, appBarConfiguration)
        binding.playToolBar.navigationIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_back_white)

        //경기 정보 표시
        play = model.selectedPlay!!
        binding.playToolBar.title = "${play.team1}  vs  ${play.team2}"
        binding.playSets.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.playSets.adapter = PlayAdapter(play, this)
    }

    /**
     * 세트 점수 아이템을 터치 시 호출
     *
     * Dialog를 이용해 터치한 세트 데이터의 점수를 수정한다.
     * 가능한 점수일 경우만 데이터를 수정한다.
     *
     * @param[set] 터치한 세트 데이터의 인덱스
     */
    override fun onTouchItem(set: Int) {
        val dialogBinding = EditDialogBinding.inflate(layoutInflater)
        dialogBinding.team1Dialog.text = play.team1
        dialogBinding.team2Dialog.text = play.team2

        //NumberPicker 설정
        val picker1 = dialogBinding.team1Picker
        val picker2 = dialogBinding.team2Picker

        //가능한 점수 리스트
        val displayList = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "10")
        picker1.displayedValues = displayList
        picker2.displayedValues = displayList

        picker1.maxValue = 9
        picker2.maxValue = 9

        //기존 점수를 초기 값으로 표시(10점일 경우 인덱스가 9)
        picker1.value = min(play.pointResult[set][0], 9)
        picker2.value = min(play.pointResult[set][1], 9)

        //팀 이름 표시
        dialogBinding.team1TimeWin.text = play.team1
        dialogBinding.team2TimeWin.text = play.team2

        //기존 점수가 없을 경우 초기 값을 4:4로
        if (play.roundResult[set] == null) {
            picker1.value = 4
            picker2.value = 4
        }

        //연장전에서 동점일 경우 기존 승리 팀 초기 선택
        if (picker1.value == picker2.value && set == 3) {
            dialogBinding.timeWin.visibility = View.VISIBLE
            if (play.roundResult[set] == 0)
                dialogBinding.team1TimeWin.isChecked = true
            else if (play.roundResult[set] == 1)
                dialogBinding.team2TimeWin.isChecked = true
        }

        //가능한 점수는 0:10(9), 1:8, 2:7, 2:6, 3:6, 3:5, 4:4, 5:5

        //team1 점수 변경 시
        picker1.setOnValueChangedListener { _, _, new ->
            //점수 합이 9가 되게 하나로 정해지는 경우
            //0-10 / 1-8 / 7-2 / 8-1 / 10-1
            if (new in listOf(0, 1, 7, 8, 9))
                picker2.value = 9 - new

            //점수 합이 8 또는 9가 되는 경우
            //2-6,7 / 3-5,6 / 4-4,(5) / 6-2,3
            if (new in listOf(2, 3, 4, 6) && (picker1.value+picker2.value) !in 8..9)
                picker2.value = 8 - new

            //5-3,5
            //5:3에서 5:5로 넘어갈 수 있도록 5:4를 임시 허용
            if (new == 5 && picker2.value !in 3..5)
                picker2.value = 3

            //연장전에서 동점일 경우 승리 팀 선택 옵션 표시
            if (set == 3 && picker1.value == picker2.value)
                dialogBinding.timeWin.visibility = View.VISIBLE
            else
                dialogBinding.timeWin.visibility = View.GONE
        }

        //team2 점수 변경 시
        picker2.setOnValueChangedListener { _, _, new ->
            if (new in listOf(0, 1, 7, 8, 9))
                picker1.value = 9 - new

            if (new in listOf(2, 3, 4, 6) && (picker1.value+picker2.value) !in 8..9)
                picker1.value = 8 - new

            if (new == 5 && picker1.value !in 3..5)
                picker1.value = 3

            if (set == 3 && picker1.value == picker2.value)
                dialogBinding.timeWin.visibility = View.VISIBLE
            else
                dialogBinding.timeWin.visibility = View.GONE
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
            val team1Point = if (picker1.value == 9) 10 else picker1.value
            val team2Point = if (picker2.value == 9) 10 else picker2.value
            val timeWin = if (dialogBinding.timeWin.visibility == View.GONE) null
                else if (dialogBinding.team1TimeWin.isChecked) 0 else if (dialogBinding.team2TimeWin.isChecked) 1 else null

            if ((picker1.value==5 && picker2.value==4) || (picker1.value==4 && picker2.value==5)) {
                //5:4는 불가능
                model.toastObserver.value = "불가능한 점수입니다"
            }
            else if (dialogBinding.timeWin.visibility == View.VISIBLE && timeWin == null) {
                //연장전 동점일 때 승리 팀을 선택하지 않았을 경우
                model.toastObserver.value = "승리한 팀을 선택해 주세요"
            }
            else {
                //Team 객체에 점수 반영
                val changeData = play.changeResult(set, listOf(team1Point, team2Point), timeWin)
                val team1 = model.getTeamWithAlias(play.team1)
                val team2 = model.getTeamWithAlias(play.team2)
                changeData.changeTeamInfo(team1, team2)

                model.changePlayList.add(play)
                model.changeTeamList.add(team1)
                model.changeTeamList.add(team2)

                binding.playSets.adapter?.notifyItemRangeRemoved(play.roundCount, 4-play.roundCount)
                binding.playSets.adapter?.notifyItemChanged(set)
                builder.dismiss()
            }
        }
    }

    /**
     * Fragment의 상호작용이 중지될 때 호출
     *
     * 데이터베이스를 업데이트하고 [MyViewModel.currentTeamList]의 순위를 재설정한다.
     */
    override fun onPause() {
        model.updateDatabase()
        super.onPause()
    }
}