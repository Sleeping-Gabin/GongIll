package com.example.myapplication.view.ui

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
import com.example.myapplication.R
import com.example.myapplication.database.entity.Team
import com.example.myapplication.databinding.AddTeamDialogBinding
import com.example.myapplication.databinding.TeamFragmentBinding
import com.example.myapplication.view.adapter.TeamPlayAdapter
import com.example.myapplication.view.model.MyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min

class TeamFragment: Fragment() {
    private val model: MyViewModel by activityViewModels()
    private lateinit var binding: TeamFragmentBinding
    private lateinit var team: Team

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }

        team = model.selectedTeam!!

        binding = TeamFragmentBinding.inflate(inflater)
        binding.teamPlayInfoView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.teamPlayInfoView.adapter = TeamPlayAdapter(team.alias, model.getTeamPlay())

        binding.teamRank.text = team.rank.toString()
        binding.teamWinLose.text = String.format("%d / %d", team.win, team.lose)
        binding.teamRoundPt.text = team.roundWin.toString()
        val point = team.point.toFloat()/team.roundCount.toFloat()
        binding.teamPt.text = if (point.isNaN()) "0" else String.format("%.1f", point)
        binding.teamDrawPt.text = team.drawRound.toString()

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.toolBar.navigationIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_back_white)
        binding.toolBar.title = model.selectedTeam?.name

        binding.toolBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.editName -> {
                    editTeamName()
                    true
                }
                R.id.delete -> {
                    deleteTeam()
                    true
                }
                else -> false
            }
        }

        return binding.root
    }

    private fun editTeamName() {
        val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)
        dialogBinding.addTeamDialogGroupName.visibility = View.GONE
        dialogBinding.addTeamDialogTeamNameText.setText(team.name)
        dialogBinding.addTeamDialogAliasText.setText(team.alias)

        dialogBinding.addTeamDialogTeamNameText.addTextChangedListener(object: TextWatcher {
            var isSame = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                dialogBinding.addTeamDialogTeamName.error = null
                val name = s?.filterNot { it.isWhitespace() }
                isSame = name?.subSequence(0, min(name.length, 4)).toString() ==
                        dialogBinding.addTeamDialogAliasText.text.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (isSame && s != null && s.filterNot { it.isWhitespace() }.length <= dialogBinding.addTeamDialogAlias.counterMaxLength)
                    dialogBinding.addTeamDialogAliasText.setText(s.filterNot { it.isWhitespace() })
            }
        })

        dialogBinding.addTeamDialogAliasText.doOnTextChanged { text, start, before, count ->
            if (text != null && text.length > dialogBinding.addTeamDialogAlias.counterMaxLength)
                dialogBinding.addTeamDialogAlias.error = getString(R.string.error_alias_maxCount)
            else
                dialogBinding.addTeamDialogAlias.error = null
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인", null)
            .setView(dialogBinding.root)
            .show()

        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var isError = dialogBinding.addTeamDialogAlias.error != null
            if (dialogBinding.addTeamDialogTeamNameText.text.isNullOrBlank()) {
                dialogBinding.addTeamDialogTeamName.error = getString(R.string.error_teamName_required)
                isError = true
            }

            if (!isError) {
                val teamName = dialogBinding.addTeamDialogTeamNameText.text.toString()
                val teamAlias = dialogBinding.addTeamDialogAliasText.text.toString()
                model.changeTeamName(team, teamName, teamAlias)
                binding.toolBar.title = teamName
                builder.dismiss()
            }
        }
    }

    private fun deleteTeam() {MaterialAlertDialogBuilder(requireContext())
        .setTitle("${team.groupName}의 팀 '${team.name}'을(를) 삭제합니다.")
        .setMessage("삭제 하면 되돌릴 수 없습니다. 해당 팀에 포함된 경기 데이터도 함께 삭제됩니다.")
        .setPositiveButton("삭제") { dialog, id ->
            model.deleteTeam(team)
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }
        .setNegativeButton("취소", null)
        .show()
    }

}