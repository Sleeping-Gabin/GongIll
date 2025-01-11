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

/**
 * 그룹의 상세 정보를 보여주는 Fragment
 *
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 * @property[binding] GroupDetailFragment의 View Binding 객체
 * @property[group] [MyViewModel.selectedGroup]에 해당하는
 * 상세 정보를 보여줄 [Group] 객체.
 */
class GroupDetailFragment: Fragment(), OnGroupTeamTouchListener {
    private val model: MyViewModel by activityViewModels()
    lateinit var binding: GroupDetailFragmentBinding
    var group: Group? = null

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
        binding = GroupDetailFragmentBinding.inflate(inflater)
        group = model.selectedGroup

        return binding.root
    }

    /**
     * View가 생성되었을 때 호출
     *
     * 툴바를 초기화하고 그룹 정보를 표시한다.
     *
     * - 뒤로 가기 버튼 동작 설정
     * - 툴바 초기화
     * - apdater를 연결하여 그룹의 팀 목록 표시
     * - 그룹 정보 (팀 수, 경기 수, 진행한 경기 수) 표시
     * - 팀 추가 버튼 클릭 listener 설정
     * - [MyViewModel.groupTeamList]을 observe하여 UI에 반영
     *
     * @param[view] 생성된 View 객체
     * @param[savedInstanceState] 이전 상태 정보를 저장한 Bundle 객체
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //뒤로 가기로 "그룹" 탭으로 이동
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }

        //툴바 초기화
        initToolbar()

        //그룹의 팀 목록을 표시
        var teamList = model.groupTeamList.value?.get(group)
        binding.groupDetailTeamsView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = GroupTeamAdapter(teamList, this, model)
        binding.groupDetailTeamsView.adapter = adapter

        //그룹의 팀 수, 경기 수, 진행한 경기 수 표시
        val groupPlays = model.getGroupPlay(group!!)
        binding.groupDetailTeamNum.text = model.getGroupTeam(group!!).size.toString()
        binding.groupDetailAllPlay.text = groupPlays.size.toString()
        binding.groupDetailFinishPlay.text = groupPlays.count { p -> p.winTeam != null }.toString()

        //팀 추가 버튼 클릭 시 팀 추가
        binding.groupDetailAddTeam.setOnClickListener {
            addTeam()
        }

        //그룹의 팀 목록이 변경될 때 반영
        model.groupTeamList.observe(viewLifecycleOwner) {
            teamList = it[group]
            adapter.changeData(teamList)
        }
    }

    /**
     * 툴바를 초기화한다.
     *
     * Navigation Controller를 연결하고,
     * 그룹 수정과 삭제 버튼에 listener를 연결한다.
     */
    private fun initToolbar() {
        //툴바에 Navigation Controller 연결
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.toolBar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(), R.drawable.arrow_back_white)
        binding.toolBar.title = model.selectedGroup?.name

        //버튼을 눌러 그룹의 정보를 변경하거나 삭제할 수 있음
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

    /**
     * [group]에 팀을 추가하기 위한 Dialog를 표시하고,
     * 입력된 정보로 팀을 생성해 데이터베이스에 저장한다.
     *
     * 별칭은 기본적으로 팀명과 동일하게 설정된다.
     */
    private fun addTeam() {
        val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)
        dialogBinding.addTeamDialogGroupNameText.setText(group!!.name)

        //팀 이름 입력 시
        dialogBinding.addTeamDialogTeamNameText.addTextChangedListener(object: TextWatcher {
            //팀 이름과 별칭의 앞 4글자가 동일한지 여부
            var isSame = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                dialogBinding.addTeamDialogTeamName.error = null
                val name = s?.filterNot { it.isWhitespace() }
                isSame = name?.subSequence(0, min(name.length, 4)).toString() ==
                        dialogBinding.addTeamDialogAliasText.text.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            //팀의 이름과 별칭이 동일했다면 새로 입력한 팀 이름에 따라 별칭을 동일하게 변경
            override fun afterTextChanged(s: Editable?) {
                if (isSame && s != null && s.filterNot { it.isWhitespace() }.length
                    <= dialogBinding.addTeamDialogAlias.counterMaxLength)
                    dialogBinding.addTeamDialogAliasText.setText(s.filterNot { it.isWhitespace() })
            }
        })

        //별칭이 너무 길 경우 에러 메시지
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

        //확인 버튼 터치 시
        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var isError = dialogBinding.addTeamDialogAlias.error != null
            //팀명을 입력하지 않으면 isError를 true로
            if (dialogBinding.addTeamDialogTeamNameText.text.isNullOrBlank()) {
                dialogBinding.addTeamDialogTeamName.error = getString(R.string.error_teamName_required)
                isError = true
            }

            if (!isError) { //문제가 없으면 Dialog의 정보로 팀 생성
                val teamName = dialogBinding.addTeamDialogTeamNameText.text.toString()
                val teamAlias = dialogBinding.addTeamDialogAliasText.text.toString()
                model.addTeam(group!!.name, teamName, teamAlias)
                builder.dismiss()
            }
        }
    }

    /**
     * [group]을 삭제하기 위한 Dialog를 표시하고, 데이터베이스에서 삭제한다.
     */
    private fun deleteGroup() {
        //Diolog를 표시하고, 동의 시 그룹 삭제
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("그룹 '${group?.name}'을(를) 삭제합니다.")
            .setMessage("삭제 하면 되돌릴 수 없습니다. 해당 그룹에 포함된 팀과 경기 데이터도 함께 삭제됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                model.deleteGroup(group!!)
                Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /**
     * [group]의 이름과 카테고리 변경을 위한 Diolog를 생성하고,
     * 입력된 정보로 데이터베이스를 업데이트한다.
     */
    private fun editGroupName() {
        val dialogBinding = AddGroupDialogBinding.inflate(layoutInflater)
        dialogBinding.addGroupDialogPlayNum.visibility = View.GONE
        dialogBinding.addGroupDialogGroupNameText.setText(group?.name)

        //카테고리 선택 창에 카테고리 목록 등록
        val categoryList = model.categoryList.value?.filterNot { it == "others" }
            ?.toCollection(arrayListOf()) ?: arrayListOf()
        val arrayAdapter = ArrayAdapter(requireContext(),
            R.layout.group_array_item, categoryList)

        //현재 카테고리와 그룹 이름을 기본값으로 표시
        dialogBinding.addGroupDialogCategoryText.setAdapter(arrayAdapter)
        if (group?.category != "others")
            dialogBinding.addGroupDialogCategoryText.setText(group?.category)

        //카테고리 목록이 변경될 때 반영
        model.categoryList.observe(viewLifecycleOwner) {
            arrayAdapter.clear()
            arrayAdapter.addAll(it.filterNot { s -> s == "others" })
        }

        dialogBinding.addGroupDialogGroupNameText.doBeforeTextChanged { _, _, _, _ ->
            dialogBinding.addGroupDialogGroupName.error = null
        }

        //Dialog 생성
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(" ")
            .setNegativeButton("취소", null)
            .setPositiveButton("확인") { dialog, _ ->
                if (dialogBinding.addGroupDialogGroupNameText.text.isNullOrBlank()) {
                    dialogBinding.addGroupDialogGroupName.error = getString(R.string.error_groupName_required)
                }
                else { //그룹 정보 변경
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

    /**
     * 팀 아이템을 터치할 시 호출
     *
     * 터치한 팀의 상세 정보를 보여주는 [TeamFragment]로 이동한다.
     *
     * @param[team] 터치한 팀의 [Team] 객체
     */
    override fun onTouchItem(team: Team) {
        model.selectedTeam = team
        Navigation.findNavController(requireActivity(), R.id.hostFragment).navigate(R.id.action_groupItemFragment_to_teamFragment)
    }
}