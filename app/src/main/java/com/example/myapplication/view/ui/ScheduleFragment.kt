package com.example.myapplication.view.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.view.listener.OnPlayDragListener
import com.example.myapplication.R
import com.example.myapplication.view.model.MyViewModel
import com.example.myapplication.view.adapter.ScheduleAdapter
import com.example.myapplication.view.callback.SimpleScheduleCallback
import com.example.myapplication.database.entity.Play
import com.example.myapplication.databinding.ScheduleFragmentBinding
import com.google.android.material.chip.ChipGroup

class ScheduleFragment: Fragment(), OnPlayDragListener {
    private val model: MyViewModel by activityViewModels()
    private lateinit var binding: ScheduleFragmentBinding
    private lateinit var pref: SharedPreferences
    private val changedPlays: ArrayList<Play> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScheduleFragmentBinding.inflate(inflater)
        pref = requireActivity().getSharedPreferences("previous_state", Context.MODE_PRIVATE)

        binding.scheduleView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ScheduleAdapter(model.currentPlayList.value, this)
        binding.scheduleView.adapter = adapter
        val dragCallback = SimpleScheduleCallback(adapter, requireContext())
        ItemTouchHelper(dragCallback).attachToRecyclerView(binding.scheduleView)

        initializeChipGroup()

        model.currentPlayList.observe(viewLifecycleOwner) {
            adapter.changeData(it)
        }

        return binding.root
    }

    private fun initializeChipGroup() {
        val adapter = (binding.scheduleView.adapter as ScheduleAdapter)
        binding.chipAll.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                (chip.parent as ChipGroup).clearCheck()
                adapter.filter = ScheduleAdapter.Filter.ALL
                adapter.filterData()
            }
        }

        binding.chipFinish.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                binding.chipYet.isChecked = false
                binding.chipAll.isChecked = false
                adapter.filter = ScheduleAdapter.Filter.FINISH
                adapter.filterData()
            }
        }

        binding.chipYet.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                binding.chipFinish.isChecked = false
                binding.chipAll.isChecked = false
                adapter.filter = ScheduleAdapter.Filter.YET
                adapter.filterData()
            }
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when(checkedIds.size) {
                0 -> group.check(binding.chipAll.id)
                1 -> pref.edit {
                    putInt("chip_id", group.checkedChipIds[0])
                }
            }
        }
    }

    override fun onDragItem(from: Play, to: Play) {
        model.changePlayList.add(from)
        model.changePlayList.add(to)
    }

    override fun onTouchItem(play: Play) {
        model.selectedPlay = play
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_scheduleFragment_to_playFragment)
    }

    override fun onResume() {
        val checked = pref.getInt("chip_id", binding.chipAll.id)
        binding.chipGroup.check(checked)
        super.onResume()
    }

    override fun onPause() {
        model.updateDatabase()
        super.onPause()
    }
}