package com.example.myapplication.view.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.view.model.MyViewModel

/**
 * 앱의 전체 화면을 구성하는 Activity 클래스
 *
 * @property[binding] MainActivity의 ViewBinding 객체
 * @property[pref] 이전 선택을 저장하는  SharedPreferences 객체.
 * "previous_state"에 선택한 그룹인 "group"과 선택한 경기 데이터 필터 chip인 "chip_id"를 저장
 * @property[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 */
class MainActivity : AppCompatActivity() {
	lateinit var binding: ActivityMainBinding
	private lateinit var pref: SharedPreferences
	private val model: MyViewModel by viewModels()
	
	/**
	 * MainActivity가 생성될 때 호출
	 *
	 * - Navigation Controller 초기화
	 * - [MyViewModel.toastObserver]를 observe하여 토스트 메시지 생성
	 * - [MyViewModel.teamList], [MyViewModel.playList],
	 * [MyViewModel.categoryList], [MyViewModel.groupList]를 observe하여
	 * 데이터베이스의 데이터를 [model]의 property로 업데이트
	 *
	 * @param[savedInstanceState] 이전 상태를 저장한 Bundle 객체
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		setTheme(R.style.AppTheme)
		
		
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		model.mediator.observe(this) {
		}
		
		//이전 상태를 저장
		//이전에 선택한 그룹: "group"
		pref = getSharedPreferences("previous_state", MODE_PRIVATE)
		
		//Navigation Controller 초기화
		initNavController()
		
		//토스트 메시지를 띄움
		model.toastObserver.observe(this) {
			Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
		}
	}
	
	/**
	 * 앱 바의 그룹 선택창을 초기화한다.
	 *
	 * [pref]에 이전 그룹 선택이 저장되어 있다면 해당 그룹으로, 아니면 첫 번째 그룹으로 설정한다.
	 * 리스트가 비어 있으면 선택되지 않는다.
	 * [MyViewModel.groupList] 값이 변경될 때마다 그룹 목록에 반영한다.
	 */
	private fun initSelectGroupText() {
		val arrayAdapter = ArrayAdapter(this, R.layout.group_array_item, arrayListOf(""))
		binding.selectGroupText.setAdapter(arrayAdapter)
		binding.selectGroupText.setText("", false)
		//목록의 아이템 선택 시, 선택한 그룹으로 텍스트 변경
		binding.selectGroupText.setOnItemClickListener { adapterView, _, position, _ ->
			val groupName = adapterView.getItemAtPosition(position).toString()
			changeSelectGroupText(groupName)
		}
		
		//그룹 리스트가 변경될 때마다 목록을 업데이트
		model.groupList.observe(this) {
			//val list = it.map { g -> if (g.category != "others") g.category + " - " + g.name else g.name }
			val list = it?.map { group -> group.name } ?: arrayListOf()
			val currentText = binding.selectGroupText.text.toString()
			
			//그룹 선택 목록 업데이트
			arrayAdapter.clear()
			arrayAdapter.addAll(list)
			
			if (list.isEmpty()) { //리스트가 비었을 때 미선택
				changeSelectGroupText("")
			} else if (currentText == "") { //현재 미선택 되었을 때
				val selectedGroup = pref.getString("group", "") //이전에 선택한 그룹
				if (list.contains(selectedGroup)) { //그룹 리스트에 있으면 해당 그룹 선택
					changeSelectGroupText(selectedGroup ?: "")
				} else { //없으면 첫번째 그룹 선택
					changeSelectGroupText(list[0])
				}
			} else if (!list.contains(currentText)) { //선택된 그룹이 리스트에 없을 때 첫번째 그룹 선택
				changeSelectGroupText(list[0])
			}
		}
	}
	
	/**
	 * 앱 바의 그룹 선택창의 텍스트를 변경한다.
	 *
	 * [MyViewModel.selectedCurrentGroup]를 변경하고 [pref]에 저장한다.
	 *
	 * @param[newText] 변경할 텍스트의 문자열.
	 * "카테고리 - 그룹" 형식으로 주어진다.
	 */
	private fun changeSelectGroupText(newText: String) {
		//텍스트 변경
		binding.selectGroupText.setText(newText, false)
		
		//텍스트의 그룹을 선택
		val txt = newText.split(" - ")
		model.selectGroup(txt.last())
		
		//선택한 그룹 기록
		pref.edit {
			putString("group", newText)
		}
	}
	
	
	/**
	 * Navigation Controller를 초기화한다.
	 *
	 * 이동하는 탭에 따라 타이틀을 변경하고 필요한 UI를 표시한다.
	 */
	private fun initNavController() {
		//ScheduleFragment, RankFragment, GroupFragment로 탭 지정
		val appBarConfiguration =
			AppBarConfiguration(setOf(R.id.scheduleFragment, R.id.groupFragment, R.id.rankFragment))
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.hostFragment) as NavHostFragment
		val navController = navHostFragment.navController
		binding.toolBar.setupWithNavController(navController, appBarConfiguration)
		binding.navBar.setupWithNavController(navController)
		//NavigationUI.setupWithNavController(binding.navBar, navController, false)
		
		//탭을 이동할 때, 해당 탭에 따른 UI 변경
		navController.addOnDestinationChangedListener { _, destination, _ ->
			when (destination.id) {
				R.id.scheduleFragment -> {
					//그룹 선택창, 상단 앱 바, 하단 탭 바 모두 표시
					binding.selectGroup.visibility = View.VISIBLE
					binding.navBar.visibility = View.VISIBLE
					binding.appBar.visibility = View.VISIBLE
					binding.toolBar.setTitle(R.string.title_schedule)
				}
				
				R.id.rankFragment -> {
					//그룹 선택창, 상단 앱 바, 하단 탭 바 모두 표시
					binding.selectGroup.visibility = View.VISIBLE
					binding.navBar.visibility = View.VISIBLE
					binding.appBar.visibility = View.VISIBLE
					binding.toolBar.setTitle(R.string.title_team)
				}
				
				R.id.groupFragment -> {
					//그룹 선택창 비 표시
					binding.selectGroup.visibility = View.GONE
					binding.navBar.visibility = View.VISIBLE
					binding.appBar.visibility = View.VISIBLE
					binding.toolBar.setTitle(R.string.title_group)
				}
				
				R.id.predictResultFragment -> {
					//그룹 선택창 비 표시, 뒤로 가기 아이콘 표시
					binding.selectGroup.visibility = View.GONE
					binding.navBar.visibility = View.VISIBLE
					binding.appBar.visibility = View.VISIBLE
					binding.toolBar.setTitle(R.string.title_result)
					binding.toolBar.navigationIcon = AppCompatResources.getDrawable(
						applicationContext, R.drawable.arrow_back_white
					)
				}
				
				else -> {
					//모두 비 표시
					binding.selectGroup.visibility = View.GONE
					binding.navBar.visibility = View.GONE
					binding.appBar.visibility = View.GONE
				}
			}
		}
	}
	
	/**
	 * 앱이 재개될 때 호출
	 *
	 * 그룹 선택창을 초기화한다.
	 */
	override fun onResume() {
		initSelectGroupText()
		super.onResume()
	}
}