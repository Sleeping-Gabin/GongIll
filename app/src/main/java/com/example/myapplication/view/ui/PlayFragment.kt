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

class PlayFragment: Fragment(), OnPointTouchListener {
    private val model: MyViewModel by activityViewModels()
    lateinit var binding: PlayFragmentBinding
    lateinit var play: Play

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayFragmentBinding.inflate(inflater)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.playToolBar.setupWithNavController(navController, appBarConfiguration)
        binding.playToolBar.navigationIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_back_white)

        play = model.selectedPlay!!
        //binding.playTitleEdit.text = "${play.team1}  vs  ${play.team2}"
        binding.playToolBar.title = "${play.team1}  vs  ${play.team2}"
        binding.playSets.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.playSets.adapter = PlayAdapter(play, this)

        return binding.root
    }

    override fun onTouchItem(play: Play, round: Int) {
        val dialogBinding = EditDialogBinding.inflate(layoutInflater)
        dialogBinding.team1Dialog.text = play.team1
        dialogBinding.team2Dialog.text = play.team2

        val picker1 = dialogBinding.team1Picker
        val picker2 = dialogBinding.team2Picker
        val displayList = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "10")
        picker1.maxValue = 9
        picker1.value = min(play.pointResult[round][0], 9)
        picker1.displayedValues = displayList
        picker2.maxValue = 9
        picker2.value = min(play.pointResult[round][1], 9)
        picker2.displayedValues = displayList

        dialogBinding.team1TimeWin.text = play.team1
        dialogBinding.team2TimeWin.text = play.team2

        if (play.roundResult[round] == null) {
            picker1.value = 4
            picker2.value = 4
        }

        if (picker1.value == picker2.value && round == 3) {
            dialogBinding.timeWin.visibility = View.VISIBLE
            if (play.roundResult[round] == 0)
                dialogBinding.team1TimeWin.isChecked = true
            else if (play.roundResult[round] == 1)
                dialogBinding.team2TimeWin.isChecked = true
        }

        picker1.setOnValueChangedListener { numberPicker, old, new ->
            if (new in listOf(0, 1, 7, 8, 9))
                picker2.value = 9 - new

            if (new in listOf(2, 3, 4, 6) && (picker1.value+picker2.value) !in 8..9)
                picker2.value = 8 - new

            if (new == 5 && picker2.value !in 3..5)
                picker2.value = 3

            if (round == 3 && picker1.value == picker2.value)
                dialogBinding.timeWin.visibility = View.VISIBLE
            else
                dialogBinding.timeWin.visibility = View.GONE
        }

        picker2.setOnValueChangedListener { numberPicker, old, new ->
            if (new in listOf(0, 1, 7, 8, 9))
                picker1.value = 9 - new

            if (new in listOf(2, 3, 4, 6) && (picker1.value+picker2.value) !in 8..9)
                picker1.value = 8 - new

            if (new == 5 && picker1.value !in 3..5)
                picker1.value = 3

            if (round == 3 && picker1.value == picker2.value)
                dialogBinding.timeWin.visibility = View.VISIBLE
            else
                dialogBinding.timeWin.visibility = View.GONE
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인", null)
            .setView(dialogBinding.root)
            .show()

        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val team1Point = if (picker1.value == 9) 10 else picker1.value
            val team2Point = if (picker2.value == 9) 10 else picker2.value
            val timeWin = if (dialogBinding.timeWin.visibility == View.GONE) null
                else if (dialogBinding.team1TimeWin.isChecked) 0 else if (dialogBinding.team2TimeWin.isChecked) 1 else null

            if ((picker1.value==5 && picker2.value==4) || (picker1.value==4 && picker2.value==5))
                model.toastObserver.value = "불가능한 점수입니다"
            else if (dialogBinding.timeWin.visibility == View.VISIBLE && timeWin == null)
                model.toastObserver.value = "승리한 팀을 선택해 주세요"
            else {
                val changeData = play.changeResult(round, listOf(team1Point, team2Point), timeWin)
                val team1 = model.getTeamWithAlias(play.team1)
                val team2 = model.getTeamWithAlias(play.team2)
                changeData.changeTeamInfo(team1, team2)

                model.changePlayList.add(play)
                model.changeTeamList.add(team1)
                model.changeTeamList.add(team2)

                binding.playSets.adapter?.notifyItemRangeRemoved(play.roundCount, 4-play.roundCount)
                binding.playSets.adapter?.notifyItemChanged(round)
                builder.dismiss()
            }
        }
    }

    override fun onPause() {
        model.updateDatabase()
        super.onPause()
    }

    override fun onDestroy() {
        model.rank()
        super.onDestroy()
    }
}