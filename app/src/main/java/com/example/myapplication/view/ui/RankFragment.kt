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

class RankFragment: Fragment(), OnTeamTouchListener{
    private val model: MyViewModel by activityViewModels()
    private lateinit var binding: RankFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RankFragmentBinding.inflate(inflater)

        binding.rankView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = RankAdapter(model.currentTeamList.value, this)
        binding.rankView.adapter = adapter

        model.currentTeamList.observe(viewLifecycleOwner) {
            adapter.changeData(it.sortedBy { play -> play.rank })
        }

        binding.rankItemHPredictBtn.setOnClickListener {
            val remain = model.currentPlayList.value!!.count { it.winIdx == null }
            if (remain > 12)
                model.toastObserver.value = "12개 이상의 경기가 남은 경우 가능성을 확인 할 수 없습니다."
            else
                showPredictDialog()
        }

        return binding.root
    }

    private fun showPredictDialog() {
        val dialogBinding = PredictDialogBinding.inflate(layoutInflater)

        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.group_array_item, model.currentTeamList.value!!.map { it.alias })
        dialogBinding.predictDialogTeamText.setAdapter(arrayAdapter)

        dialogBinding.predictDialogRankText.doOnTextChanged { text, start, before, count ->
            dialogBinding.predictDialogRank.error = null
            if ((text.toString().toIntOrNull() ?: Int.MAX_VALUE) > model.currentTeamList.value!!.size)
                dialogBinding.predictDialogRank.error = getString(R.string.error_rank_max)
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("가능성 확인하기", null)
            .setView(dialogBinding.root)
            .show()

        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var isError = dialogBinding.predictDialogRank.error != null
            if (dialogBinding.predictDialogTeamText.text.isNullOrBlank()) {
                dialogBinding.predictDialogTeam.error = getString(R.string.error_team_required)
                isError = true
            }
            if (dialogBinding.predictDialogRankText.text.isNullOrBlank()) {
                dialogBinding.predictDialogRank.error = getString(R.string.error_rank_required)
                isError = true
            }

            if (!isError) {
                val team = dialogBinding.predictDialogTeamText.text.toString()
                val rank = dialogBinding.predictDialogRankText.text.toString().toInt()

                val action = RankFragmentDirections.actionRankFragmentToPredictFragment(team, rank)
                Navigation.findNavController(requireActivity(), R.id.hostFragment)
                    .navigate(action)
                builder.dismiss()
            }
        }
    }

    override fun onTouchItem(team: Team) {
        model.selectedTeam = team
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_rankFragment_to_teamFragment)
    }
}