package com.example.myapplication.view.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.entity.Group
import com.example.myapplication.databinding.AddGroupDialogBinding
import com.example.myapplication.databinding.AddTeamDialogBinding
import com.example.myapplication.databinding.GroupFragmentBinding
import com.example.myapplication.view.adapter.CategoryAdapter
import com.example.myapplication.view.listener.OnGroupTouchListener
import com.example.myapplication.view.model.MyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min

class GroupFragment : Fragment(), OnGroupTouchListener {
    private val model: MyViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = GroupFragmentBinding.inflate(inflater)
        binding.groupView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = CategoryAdapter(model.categoryGroupList.value, this, model)
        binding.groupView.adapter = adapter

        model.categoryGroupList.observe(viewLifecycleOwner) {
            adapter.changeData(it)
        }

        return binding.root
    }

    override fun onTouchItem(group: Group) {
        model.selectedGroup = group
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_groupFragment_to_groupItemFragment)
    }

    override fun onTouchButton(group: Group) {
        val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)
        dialogBinding.addTeamDialogGroupNameText.setText(group.name)

        dialogBinding.addTeamDialogTeamNameText.addTextChangedListener(object: TextWatcher {
            var isSame = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                dialogBinding.addTeamDialogTeamName.error = null
                val name = s?.filterNot { it.isWhitespace() }
                isSame = name?.subSequence(0, min(name.length, dialogBinding.addTeamDialogAlias.counterMaxLength)).toString() ==
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
                model.addTeam(group.name, teamName, teamAlias)
                builder.dismiss()
            }
        }
    }

    override fun onTouchHeader(category: String) {
        val dialogBinding = AddGroupDialogBinding.inflate(layoutInflater)

        val categoryList = model.categoryList.value?.filterNot { it == "others" }?.toCollection(arrayListOf()) ?: arrayListOf()
        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.group_array_item, categoryList)
        dialogBinding.addGroupDialogCategoryText.setAdapter(arrayAdapter)
        if (category != "others")
            dialogBinding.addGroupDialogCategoryText.setText(category)

        model.categoryList.observe(this) {
            arrayAdapter.clear()
            arrayAdapter.addAll(it.filterNot { it == "others" })
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인", null)
            .setView(dialogBinding.root)
            .show()

        dialogBinding.addGroupDialogGroupNameText.doBeforeTextChanged { text, start, count, after ->
            dialogBinding.addGroupDialogGroupName.error = null
        }

        dialogBinding.addGroupDialogPlayNumText.doBeforeTextChanged { text, start, count, after ->
            dialogBinding.addGroupDialogPlayNum.error = null
        }

        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var isError = false
            if (dialogBinding.addGroupDialogGroupNameText.text.isNullOrBlank()) {
                dialogBinding.addGroupDialogGroupName.error = getString(R.string.error_groupName_required)
                isError = true
            }
            if (dialogBinding.addGroupDialogPlayNumText.text.isNullOrBlank()) {
                dialogBinding.addGroupDialogPlayNum.error = getString(R.string.error_playNum_min)
                isError = true
            }
            else if (dialogBinding.addGroupDialogPlayNumText.text.toString().toInt() < 1) {
                dialogBinding.addGroupDialogPlayNum.error = getString(R.string.error_playNum_min)
                isError = true
            }

            if (!isError) {
                val newCategory = dialogBinding.addGroupDialogCategoryText.text.toString()
                val groupName = dialogBinding.addGroupDialogGroupNameText.text.toString()
                val playNum = dialogBinding.addGroupDialogPlayNumText.text.toString().toInt()
                model.addGroup(groupName, playNum, newCategory)
                builder.dismiss()
            }
        }
    }
}
