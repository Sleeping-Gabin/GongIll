package com.example.myapplication.view.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Group
import com.example.myapplication.database.entity.Team
import com.example.myapplication.databinding.AddGroupDialogBinding
import com.example.myapplication.databinding.AddTeamDialogBinding
import com.example.myapplication.databinding.GroupDetailFragmentBinding
import com.example.myapplication.view.adapter.GroupTeamAdapter
import com.example.myapplication.view.listener.OnGroupTeamTouchListener
import com.example.myapplication.view.model.MyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min

class GroupDetailFragment: Fragment(), OnGroupTeamTouchListener {
    private val model: MyViewModel by activityViewModels()
    lateinit var binding: GroupDetailFragmentBinding
    var group: Group? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupDetailFragmentBinding.inflate(inflater)
        group = model.selectedGroup

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }

        initToolbar()

        var teamList = model.groupTeamList.value?.get(group)
        binding.groupDetailTeamsView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = GroupTeamAdapter(teamList, this, model)
        binding.groupDetailTeamsView.adapter = adapter

        val groupPlays = model.getGroupPlay(group!!)
        binding.groupDetailTeamNum.text = model.getGroupTeam(group!!).size.toString()
        binding.groupDetailAllPlay.text = groupPlays.size.toString()
        binding.groupDetailFinishPlay.text = groupPlays.count { p -> p.winTeam != null }.toString()

        binding.groupDetailAddTeam.setOnClickListener {
            addTeam()
        }

        model.groupTeamList.observe(viewLifecycleOwner) {
            teamList = it.get(group)
            adapter.changeData(teamList)
        }

        return binding.root
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.toolBar.navigationIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_back_white)
        binding.toolBar.title = model.selectedGroup?.name

        binding.toolBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.editName -> {
                    editGroupName()
                    true
                }
                R.id.delete -> {
                    deleteGroup()
                    true
                }
                else -> false
            }
        }
    }

    private fun addTeam() {
        val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)
        dialogBinding.addTeamDialogGroupNameText.setText(group!!.name)

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
                model.addTeam(group!!.name, teamName, teamAlias)
                builder.dismiss()
            }
        }
    }

    private fun deleteGroup() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("그룹 '${group?.name}'을(를) 삭제합니다.")
            .setMessage("삭제 하면 되돌릴 수 없습니다. 해당 그룹에 포함된 팀과 경기 데이터도 함께 삭제됩니다.")
            .setPositiveButton("삭제") { dialog, id ->
                model.deleteGroup(group!!)
                Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun editGroupName() {
        val dialogBinding = AddGroupDialogBinding.inflate(layoutInflater)
        dialogBinding.addGroupDialogPlayNum.visibility = View.GONE
        dialogBinding.addGroupDialogGroupNameText.setText(group?.name)

        val categoryList = model.categoryList.value?.filterNot { it == "others" }?.toCollection(arrayListOf()) ?: arrayListOf()
        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.group_array_item, categoryList)
        dialogBinding.addGroupDialogCategoryText.setAdapter(arrayAdapter)
        if (group?.category != "others")
            dialogBinding.addGroupDialogCategoryText.setText(group?.category)

        model.categoryList.observe(viewLifecycleOwner) {
            arrayAdapter.clear()
            arrayAdapter.addAll(it.filterNot { it == "others" })
        }

        dialogBinding.addGroupDialogGroupNameText.doBeforeTextChanged { text, start, count, after ->
            dialogBinding.addGroupDialogGroupName.error = null
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인") { dialog, id ->
                if (dialogBinding.addGroupDialogGroupNameText.text.isNullOrBlank()) {
                    dialogBinding.addGroupDialogGroupName.error = getString(R.string.error_groupName_required)
                }
                else {
                    val category = dialogBinding.addGroupDialogCategoryText.text.toString()
                    val groupName = dialogBinding.addGroupDialogGroupNameText.text.toString()
                    model.changeGroupCategoryAndName(group!!, category, groupName)
                    binding.toolBar.title = groupName
                    dialog.dismiss()
                }
            }
            .setView(dialogBinding.root)
            .show()
    }

    override fun onTouchItem(team: Team) {
        model.selectedTeam = team
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_groupItemFragment_to_teamFragment)
    }
}