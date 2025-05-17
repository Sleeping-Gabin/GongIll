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

/**
 * 그룹 목록을 보여주는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 * @property[binding] GroupFragment의 ViewBinding 객체
 */
class GroupFragment : Fragment(), OnGroupTouchListener {
    private val model: MyViewModel by activityViewModels()
    lateinit var binding: GroupFragmentBinding

    /**
     * GroupFragment의 View를 생성
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
        binding = GroupFragmentBinding.inflate(inflater)

        return binding.root
    }

    /**
     * View가 생성되었을 때 호출된다.
     *
     * dapter를 연결하여 카테고리별 그룹 목록을 표시하고,
     * [MyViewModel.categoryGroupList]를 observe하여 데이터 변경 사항을 UI에 반영한다.
     *
     * @param[view] 생성된 View
     * @param[savedInstanceState] 프래그먼트의 이전 상태 정보
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.groupView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = CategoryAdapter(model.categoryGroupList.value, this, model)
        binding.groupView.adapter = adapter

        //카테고리별 그룹 목록이 변경될 때 반영
        model.categoryGroupList.observe(viewLifecycleOwner) {
            adapter.changeData(it)
        }

        model.groupPlayList.observe(viewLifecycleOwner) {
            adapter.changeData(model.categoryGroupList.value)
        }
    }

    /**
     * 그룹의 팀 추가 버튼을 누를 시 호출
     *
     * 그룹에 팀을 추가하기 위한 Dialog를 생성하고,
     * 입력한 정보로 팀을 생성한다.
     *
     * @param[group] 팀 추가 버튼을 터치한 그룹의 [Group] 객체
     */
    override fun onTouchButton(group: Group) {
        val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)

        //그룹 이름은 선택한 그룹의 이름으로 고정
        dialogBinding.addTeamDialogGroupNameText.setText(group.name)

        //팀 이름 입력시
        dialogBinding.addTeamDialogTeamNameText.addTextChangedListener(object: TextWatcher {
            //팀 이름과 별칭의 앞 4글자가 동일한지 여부
            var isSame = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                dialogBinding.addTeamDialogTeamName.error = null
                val name = s?.filterNot { it.isWhitespace() }
                isSame = name?.subSequence(0, min(name.length, dialogBinding.addTeamDialogAlias.counterMaxLength)).toString() ==
                        dialogBinding.addTeamDialogAliasText.text.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            //팀의 이름과 별칭이 동일했다면 새로 입력한 팀 이름에 따라 별칭을 동일하게 변경
            override fun afterTextChanged(s: Editable?) {
                if (isSame && s != null && s.filterNot { it.isWhitespace() }.length <= dialogBinding.addTeamDialogAlias.counterMaxLength)
                    dialogBinding.addTeamDialogAliasText.setText(s.filterNot { it.isWhitespace() })
            }
        })

        //별칭이 너무 길 경우 경우 에러 메시지
        dialogBinding.addTeamDialogAliasText.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.length > dialogBinding.addTeamDialogAlias.counterMaxLength)
                dialogBinding.addTeamDialogAlias.error = getString(
                    R.string.error_alias_maxCount
                    , dialogBinding.addTeamDialogAlias.counterMaxLength)
            else
                dialogBinding.addTeamDialogAlias.error = null
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
            var isError = dialogBinding.addTeamDialogAlias.error != null
            //팀명을 입력하지 않으면 isError를 true로
            if (dialogBinding.addTeamDialogTeamNameText.text.isNullOrBlank()) {
                dialogBinding.addTeamDialogTeamName.error = getString(R.string.error_teamName_required)
                isError = true
            }

            if (!isError) { //문제가 없으면 팀 생성
                val teamName = dialogBinding.addTeamDialogTeamNameText.text.toString()
                val teamAlias = dialogBinding.addTeamDialogAliasText.text.toString()
                model.addTeam(group.name, teamName, teamAlias)
                builder.dismiss()
            }
        }
    }

    /**
     * 그룹 아이템을 터치할 시 호출
     *
     * 터치한 그룹의 상세 정보를 보여주는 Fragment로 이동한다.
     *
     * @param[group] 터치한 그룹의 [Group] 객체
     */
    override fun onTouchItem(group: Group) {
        model.selectedGroup = group
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_groupFragment_to_groupItemFragment)
    }

    /**
     * 그룹 추가 버튼을 터치할 시 호출
     *
     * 카테고리에 그룹을 추가하기 위한 Dialog를 생성하고,
     * 입력한 정보로 그룹 생성한다.
     *
     * @param[category] 그룹 추가 버튼을 터치한 카테고리의 이름
     */
    override fun onTouchHeader(category: String) {
        val dialogBinding = AddGroupDialogBinding.inflate(layoutInflater)

        //카테고리 입력 창에 카테고리 목록 연결
        val categoryList = model.categoryList.value?.filterNot { it == "others" }
            ?.toCollection(arrayListOf()) ?: arrayListOf()
        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.group_array_item, categoryList)
        dialogBinding.addGroupDialogCategoryText.setAdapter(arrayAdapter)
        if (category != "others")
            dialogBinding.addGroupDialogCategoryText.setText(category)

        //카테고리 목록 변경 시 반영
        model.categoryList.observe(this) {
            arrayAdapter.clear()
            arrayAdapter.addAll(it.filterNot { s -> s == "others" })
        }

        //Dialog 생성
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인", null)
            .setView(dialogBinding.root)
            .show()

        dialogBinding.addGroupDialogGroupNameText.doBeforeTextChanged { _, _, _, _ ->
            dialogBinding.addGroupDialogGroupName.error = null
        }

        dialogBinding.addGroupDialogPlayNumText.doBeforeTextChanged { _, _, _, _ ->
            dialogBinding.addGroupDialogPlayNum.error = null
        }

        //확인 버튼을 누를 시
        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var isError = false

            //그룹명 미 입력시 isError를 true로
            if (dialogBinding.addGroupDialogGroupNameText.text.isNullOrBlank()) {
                dialogBinding.addGroupDialogGroupName.error = getString(R.string.error_groupName_required)
                isError = true
            }

            //사이클 수 미 입력시 isError를 true로
            if (dialogBinding.addGroupDialogPlayNumText.text.isNullOrBlank()) {
                dialogBinding.addGroupDialogPlayNum.error = getString(R.string.error_playNum_min)
                isError = true
            }

            //사이클 수가 1 미만일 경우 isError를 true로
            else if (dialogBinding.addGroupDialogPlayNumText.text.toString().toInt() < 1) {
                dialogBinding.addGroupDialogPlayNum.error = getString(R.string.error_playNum_min)
                isError = true
            }

            if (!isError) { //문제가 없으면 그룹 생성
                val newCategory = dialogBinding.addGroupDialogCategoryText.text.toString()
                val groupName = dialogBinding.addGroupDialogGroupNameText.text.toString()
                val playNum = dialogBinding.addGroupDialogPlayNumText.text.toString().toInt()
                model.addGroup(groupName, playNum, newCategory)
                builder.dismiss()
            }
        }
    }
}
