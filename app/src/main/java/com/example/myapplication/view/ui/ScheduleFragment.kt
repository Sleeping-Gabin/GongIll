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

/**
 * 경기 일정을 보여주는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 * @property[binding] ScheduleFragment의 ViewBinding 객체
 * @property[pref] 이전 선택을 저장하는  SharedPreferences 객체.
 * "previous_state"에 선택한 그룹인 "group"과 선택한 경기 데이터 필터 chip인 "chip_id"를 저장
 */
class ScheduleFragment: Fragment(), OnPlayDragListener {
    private val model: MyViewModel by activityViewModels()
    private lateinit var binding: ScheduleFragmentBinding
    private lateinit var pref: SharedPreferences
    private val changedPlays: ArrayList<Play> = arrayListOf()

    /**
     * GroupDetailFragment의 View를 생성
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
        binding = ScheduleFragmentBinding.inflate(inflater)
        pref = requireActivity().getSharedPreferences("previous_state", Context.MODE_PRIVATE)

        binding.scheduleView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ScheduleAdapter(model.currentPlayList.value, this)
        binding.scheduleView.adapter = adapter

        //경기 목록의 recycler view에 ItemTouchHelper를 연결
        val dragCallback = SimpleScheduleCallback(adapter, requireContext())
        ItemTouchHelper(dragCallback).attachToRecyclerView(binding.scheduleView)

        //필터 chip 초기화
        initializeChipGroup()

        //현재 경기 목록이 변경될 때마다 adapter의 데이터를 업데이트
        model.currentPlayList.observe(viewLifecycleOwner) {
            adapter.changeData(it)
        }

        return binding.root
    }

    /**
     * 경기 필터 Chip의 listener를 초기화한다.
     * 선택한 필터에 따라 경기 목록을 표시하고 Chip의 ID를 [pref]에 저장한다.
     */
    private fun initializeChipGroup() {
        val adapter = (binding.scheduleView.adapter as ScheduleAdapter)

        //'모든 경기' 선택
        binding.chipAll.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                (chip.parent as ChipGroup).clearCheck()
                adapter.filter = ScheduleAdapter.Filter.ALL
                adapter.filterData()
            }
        }

        binding.chipFinish.setOnCheckedChangeListener { chip, isChecked ->
        //'진행한 경기' 선택
            if (isChecked) {
                binding.chipYet.isChecked = false
                binding.chipAll.isChecked = false
                adapter.filter = ScheduleAdapter.Filter.FINISH
                adapter.filterData()
            }
        }

        binding.chipYet.setOnCheckedChangeListener { chip, isChecked ->
        //'남은 경기' 선택
            if (isChecked) {
                binding.chipFinish.isChecked = false
                binding.chipAll.isChecked = false
                adapter.filter = ScheduleAdapter.Filter.YET
                adapter.filterData()
            }
        }

        //선택한 chip 저장
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when(checkedIds.size) {
                0 -> group.check(binding.chipAll.id)
                1 -> pref.edit {
                    putInt("chip_id", group.checkedChipIds[0])
                }
            }
        }
    }

    /**
     * 경기 데이터 아이템을 드래그할 때 호출
     *
     * 두 경기 아이템의 위치를 바꾼다.
     *
     * @param[from] 움직인 경기 데이터의 [Play] 객체
     * @param[to] 바꿀 위치의 [Play] 객체
     */
    override fun onDragItem(from: Play, to: Play) {
        model.changePlayList.add(from)
        model.changePlayList.add(to)
    }

    /**
     * 경기 데이터 수정 버튼 터치 시 호출
     *
     * 선택한 경기 데이터의 결과를 수정하는 [PlayFragment]로 이동한다.
     *
     * @param[play] 데이터를 수정할 경기의 [Play] 객체
     */
    override fun onTouchItem(play: Play) {
        model.selectedPlay = play
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_scheduleFragment_to_playFragment)
    }

    /**
     * Fragment가 재개될 때 호출
     *
     * 이전에 선택한 경기 필터 Chip을 선택한다.
     */
    override fun onResume() {
        val checked = pref.getInt("chip_id", binding.chipAll.id)
        binding.chipGroup.check(checked)
        super.onResume()
    }

    /**
     * Fragment이 중지될 때 호출
     *
     * 데이터베이스를 업데이트한다.
     */
    override fun onPause() {
        model.updateDatabase()
        super.onPause()
    }
}