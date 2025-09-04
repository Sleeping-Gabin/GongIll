##  소개
‘제5인격’이라는 게임의 e-sports 대회의 경기 결과를 기록하고,  
응원하는 팀이 목표 순위에 들 수 있는 시나리오를 탐색하는 앱  
<br>

### 기획서
[기획서](https://github.com/Sleeping-Gabin/GongIll/raw/main/ui_plan.pdf)  
<br>

### 사용 기술
![kotlin](https://img.shields.io/badge/kotlin-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![android studio](https://img.shields.io/badge/android_studio-3DDC84.svg?style=for-the-badge&logo=androidstudio&logoColor=white)  
<br>

### 프로젝트 기간
2023.03 ~ 2024.11  

<br><br>

## 페이지
|    스플래시    |   경기 목록    |     순위     |    그룹 목록     |
|:----------:|:----------:|:----------:|:------------:|
|![스플래시](https://github.com/user-attachments/assets/3fcf8c66-f2d0-4e32-82c7-5819678d754a) |![경기 목록](https://github.com/user-attachments/assets/f9245d06-d6ed-4fbf-9e24-7bc43c1e8c90) |![순위](https://github.com/user-attachments/assets/5901c635-b62f-4003-b1bc-d0d81150c746) |![그룹 목록](https://github.com/user-attachments/assets/0c72c5b1-a220-4a76-9983-9745c56d1efe) |
|   경기 상세    |    팀 상세    |   그룹 상세    |   시나리오 분석    |
|![경기 상세](https://github.com/user-attachments/assets/edf578af-38a2-4305-a365-cd2c730fda41) |![팀 상세](https://github.com/user-attachments/assets/daee6e7d-0d1b-4016-a383-75b794f6a050) |![그룹 상세](https://github.com/user-attachments/assets/545733ab-34f9-45fc-beba-225cbaeb6d89) |![시나리오 분석](https://github.com/user-attachments/assets/8e9b62fb-494e-42ae-ac36-d64681aff6b2) |

<br><br>

## 기능
### 경기 일정 필터링
![필터링](https://github.com/user-attachments/assets/05fea455-2be2-4c6e-bcca-087fb4bda1c1)

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.ui.ScheduleFragment

private fun initializeChipGroup() {
	val adapter = (binding.scheduleView.adapter as ScheduleAdapter)
	
	binding.chipAll.setOnCheckedChangeListener { chip, isChecked ->
		if (isChecked) {
			(chip.parent as ChipGroup).clearCheck()
			adapter.filter = ScheduleAdapter.Filter.ALL
			adapter.filterData()
		}
	}
	
	// ...
}
```

```kotlin
//com.gabin.gongill.view.adapter.ScheduleAdapter

fun filterData() {
	filterList = when (filter) {
		Filter.ALL -> ArrayList(playList)
		Filter.YET -> playList.filter { p -> p.winTeam == null }.toCollection(ArrayList())
		Filter.FINISH -> playList.filter { p -> p.winTeam != null }.toCollection(ArrayList())
	}
	filterList.sortBy { it.order }
	notifyDataSetChanged()
}
```
</details>

필터링 칩을 선택하면 경기 일정을 필터링 해 보여준다.  
<br>

### 일정 터치, 드래그, 슬라이드
|터치   |드래그   |슬라이드   |
|:---:|:---:|:---:|
|![터치](https://github.com/user-attachments/assets/557e53c0-59b9-4e8f-9391-6b6f08c45578) |![드래그](https://github.com/user-attachments/assets/09c76186-3d2b-491a-9dca-a888b14ee30f) |![슬라이드](https://github.com/user-attachments/assets/4c67d945-995f-487d-b360-7adb9867214f) |

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.adapter.ScheduleAdapter

fun dragItem(from: Int, to: Int) {
	val fromPlay = filterList[from]
	val toPlay = filterList[to]
	changeItemOrder(fromPlay, toPlay)
	
	val fromIdx = playList.indexOf(fromPlay)
	val toIdx = playList.indexOf(toPlay)
	Collections.swap(playList, fromIdx, toIdx)
	
	Collections.swap(filterList, from, to)
	notifyItemMoved(from, to)
	listener.onDragItem(fromPlay, toPlay)
}
```

```kotlin
//com.gabin.gongill.view.callback.SimpleSwipeCallback

class SimpleScheduleCallback(private val adapter: ScheduleAdapter, context: Context) :
	ItemTouchHelper.SimpleCallback(
		ItemTouchHelper.UP or ItemTouchHelper.DOWN,
		ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
	) {
	
	// ...

	override fun onChildDraw(
		c: Canvas,
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		dX: Float,
		dY: Float,
		actionState: Int,
		isCurrentlyActive: Boolean
	) {
		when (actionState) {
			ItemTouchHelper.ACTION_STATE_SWIPE -> {
				val holder = viewHolder as ScheduleAdapter.ViewHolder
				if (holder != currentSwipeHolder) {
					if (currentSwipeHolder != null) {
						getDefaultUIUtil().onDraw(
							c,
							recyclerView,
							currentSwipeHolder!!.binding.scheduleFrame,
							0f,
							currentY,
							actionState,
							isCurrentlyActive
						)
						currentSwipeHolder!!.isSwiped = false
					}
					currentSwipeHolder = holder
				}
				
				var x = dX
				
				if (holder.isSwiped) {
					x = if (isCurrentlyActive)
						-maxSwipe + dX
					else
						min(dX, -maxSwipe)
				}
				x = min(x, 0f)
				
				currentX = x
				currentY = dY
				
				getDefaultUIUtil().onDraw(
					c,
					recyclerView,
					holder.binding.scheduleFrame,
					x,
					dY,
					actionState,
					isCurrentlyActive
				)
			}
			
			else -> super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
		}
	}
}
```
</details>

일정을 터치하면 라운드 점수를 확인할 수 있다.  
일정을 드래그 해 순서를 변경할 수 있다.  
일정을 왼쪽으로 슬라이드 하면 점수 수정 화면으로 이동할 수 있다.  
<br>

### 점수 수정
![점수 수정](https://github.com/user-attachments/assets/f3f2573d-70d6-4d03-ba6c-52b409a5ca38)

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.ui.PlayFragment

override fun onTouchItem(set: Int) {
	val dialogBinding = EditDialogBinding.inflate(layoutInflater)
	dialogBinding.team1Dialog.text = play.team1
	dialogBinding.team2Dialog.text = play.team2
	
	// ... NumberPicker 설정

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
		
		if ((picker1.value == 5 && picker2.value == 4) || (picker1.value == 4 && picker2.value == 5)) {
			model.toastObserver.value = "불가능한 점수입니다"
		} else if (dialogBinding.timeWin.visibility == View.VISIBLE && timeWin == null) {
			model.toastObserver.value = "승리한 팀을 선택해 주세요"
		} else {
			val changeData = play.changeResult(set, listOf(team1Point, team2Point), timeWin)
			val team1 = model.getTeamWithAlias(play.team1)
			val team2 = model.getTeamWithAlias(play.team2)
			changeData.changeTeamInfo(team1, team2)
			
			model.changePlayList.add(play)
			model.changeTeamList.add(team1)
			model.changeTeamList.add(team2)
			
			binding.playSets.adapter?.notifyItemRangeRemoved(play.roundCount, 4 - play.roundCount)
			binding.playSets.adapter?.notifyItemChanged(set)
			builder.dismiss()
		}
	}
}
```

```kotlin
//com.gabin.gongill.objects.ChangeData

class ChangeData(val play: Play) {
	private var winChange = mutableListOf(0, 0)
	private var roundChange = mutableListOf(0, 0)
	private var pointChange = mutableListOf(0, 0)
	private var drawChange = mutableListOf(0, 0)
	private var countChange = mutableListOf(0, 0)
	
	init {
		previousData()
	}
	
	private fun previousData() {
		val winIdx = play.winIdx
		if (play.winIdx == null)
			return
		
		winChange[winIdx!!] -= 1 
		
		val round1 = play.roundResult.count { result -> result == 0 }
		val round2 = play.roundResult.count { result -> result == 1 }
		roundChange[0] -= round1 - round2
		roundChange[1] -= round2 - round1
		
		pointChange[0] -= play.pointResult.take(3).sumOf { result -> result[0] }
		pointChange[1] -= play.pointResult.take(3).sumOf { result -> result[1] }
		
		val roundDraw = play.roundResult.take(3).count { result -> result == 2 }
		drawChange[winIdx!!] += roundDraw
		drawChange[1 - winIdx!!] -= roundDraw
		
		countChange[0] -= min(play.roundCount, 3)
		countChange[1] -= min(play.roundCount, 3)
	}
	
	fun changedData() {
		// ... previousData와 반대로 변경된 점수를 추가
	}
	
	fun changeTeamInfo(team1: Team?, team2: Team?) {
		// ...
	}
}
```
</details>

`NumberPicker`로 점수를 수정한다.  
한 팀의 점수를 변경하면 반대 팀의 점수가 자동으로 반영된다.  
입력한 점수에 따라 연장전까지 입력란이 증가한다.  
<br>

### 시나리오 분석
![시나리오 분석](https://github.com/user-attachments/assets/80683f8c-a778-4c04-8ee9-bb44e18d4d7a)

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.objects.PredickRank

fun predict(): PredictResult {
	exploreScenarios(Scenario(finishedResult, mutableListOf()), 0)
	
	if ((1 shl remainPlay.size) - winScenarios.size - roundScenarios.size < winScenarios.size) {
		reverse = true
		winScenarios = failScenario.toHashSet()
	}
	
	progress = 20
	update(progress)
	
	var scenarios = winScenarios
	while (scenarios.isNotEmpty()) {
		scenarios = mergeDiffOne(scenarios, "win")
		
		progress += (40f / remainPlay.size).toInt()
		update(progress)
	}
	
	progress = 60
	update(progress)
	
	// ... roundScenarios로 동일하게 진행
	
	return PredictResult(
		teams,
		winScenarios.toMutableList(),
		roundScenarios.toMutableList(),
		reverse
	)
}

private fun exploreScenarios(
	scenario: Scenario,
	depth: Int
) {
	if (depth == remainPlay.size) {
		evaluateScenario(scenario)
		return
	}
	
	val team1Idx = remainPlay[depth].team1Idx
	val team2Idx = remainPlay[depth].team2Idx
	
	scenario.teamResults.add(GameResult(team1Idx, team2Idx, remainPlay[depth].playNum, team1Idx))
	exploreScenarios(scenario, depth + 1)
	scenario.teamResults.removeAt(depth)
	
	scenario.teamResults.add(GameResult(team1Idx, team2Idx, remainPlay[depth].playNum, team2Idx))
	exploreScenarios(scenario, depth + 1)
	scenario.teamResults.removeAt(depth)
}

private fun mergeDiffOne(scenarios: HashSet<Scenario>, type: String): HashSet<Scenario> {
	val scenariosToRemove = hashSetOf<Scenario>()
	val mergeScenarios = hashSetOf<Scenario>()
	
	for (scenario in scenarios) {
		for (other in scenarios) {
			if (scenario == other) continue
			
			val diffIdx = scenario.diffResultOne(other)
			if (diffIdx != -1) {
				scenariosToRemove.add(scenario)
				scenariosToRemove.add(other)
				
				val mergeScenario = Scenario(finishedResult, scenario.teamResults.toMutableList()).apply {
					teamResults.removeAt(diffIdx)
				}
				mergeScenarios.add(mergeScenario)
			}
		}
	}
	
	// ...
	
	return mergeScenarios
}
```
</details>

응원하는 팀과 목표 순위를 지정하면 해당 순위 내에 들기 위한 시나리오(남은 경기들을 진행했을 때 발생할 수 있는 순위 결과)를 탐색한다.  
가능한 모든 결과를 탐색하고, 병합 가능한 시나리오를 병합한다.  
분석 결과에 따라 확정적으로 성공하는 경우와 라운드 비교가 필요한 경우를 나눠서 표시한다.  
<br>

### 팀 정보 수정 / 삭제
![팀 수정](https://github.com/user-attachments/assets/3aa682e9-137c-45ec-852c-1f0000f4f270)

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.ui.TeamFragment

private fun editTeamName() {
	val dialogBinding = AddTeamDialogBinding.inflate(layoutInflater)
	dialogBinding.addTeamDialogGroupName.visibility = View.GONE
	
	dialogBinding.addTeamDialogTeamNameText.setText(team.name)
	dialogBinding.addTeamDialogAliasText.setText(team.alias)
	
	dialogBinding.addTeamDialogTeamNameText.addTextChangedListener(object : TextWatcher {
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
			if (isSame && s != null && s.filterNot { it.isWhitespace() }.length
				<= dialogBinding.addTeamDialogAlias.counterMaxLength
			) {
				dialogBinding.addTeamDialogAliasText.setText(s.filterNot { it.isWhitespace() })
			}
		}
	})
	
	dialogBinding.addTeamDialogAliasText.doOnTextChanged { text, _, _, _ ->
		if (text != null && text.length > dialogBinding.addTeamDialogAlias.counterMaxLength)
			dialogBinding.addTeamDialogAlias.error = getString(
				R.string.error_alias_maxCount,
				dialogBinding.addTeamDialogAlias.counterMaxLength
			)
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
```

```kotlin
//com.gabin.gongill.view.model.MyViewModel

fun changeTeamName(team: Team, teamName: String, teamAlias: String) {
	database.changeTeamName(team, teamName, teamAlias)
}
```
</details>

팀 정보를 수정, 삭제 할 수 있다.  
기본적으로 팀의 별칭은 팀의 이름을 자동으로 따라간다.  
<br>

### 그룹/팀 추가
![그룹 추가](https://github.com/user-attachments/assets/3db06865-716b-4a86-9934-84f56ce4ed1d)

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.ui.GroupFragment

override fun onTouchHeader(category: String) {
	val dialogBinding = AddGroupDialogBinding.inflate(layoutInflater)
	
	// ... 카테고리 목록 adapter 설정
	
	val builder = MaterialAlertDialogBuilder(requireContext())
		.setTitle(" ")
		.setNegativeButton("취소", null)
		.setPositiveButton("확인", null)
		.setView(dialogBinding.root)
		.show()
	
	builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
		var isError = false
		
		// ... error 확인
		
		if (!isError) {
			val newCategory = dialogBinding.addGroupDialogCategoryText.text.toString()
			val groupName = dialogBinding.addGroupDialogGroupNameText.text.toString()
			val playNum = dialogBinding.addGroupDialogPlayNumText.text.toString().toInt()
			model.addGroup(groupName, playNum, newCategory)
			builder.dismiss()
		}
	}
}
```

```kotlin
// com.gabin.gongill.view.model.MyViewModel

fun addGroup(groupName: String, playNum: Int, category: String) {
	viewModelScope.launch {
		if (category == "others") {
			toastObserver.value = "해당 카테고리 이름은 사용할 수 없습니다."
			cancel()
		}
		
		val newGroup = Group(groupName, playNum, RankRule.COA8)
		if (category.isNotBlank())
			newGroup.category = category
		
		val result = CoroutineScope(Dispatchers.IO).async { database.addGroup(newGroup) }
		if (result.await() < 0)
			toastObserver.value = "이미 존재하는 그룹입니다."
	}
}
```
</details>

카테고리를 지정하고 그룹을 추가한다.  
각 그룹에 팀을 추가할 수 있다.  
<br>

### 그룹 정보 수정 / 삭제
![그룹 삭제](https://github.com/user-attachments/assets/ef023ee0-e208-4e1a-85b0-ef8544d41ac5)

<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.ui.GroupDetailFragment

private fun deleteGroup() {
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
```

```kotlin
//com.gabin.gongill.view.model.MyViewModel

fun deleteGroup(group: Group) {
	database.deleteGroup(group)
}
```
</details>

그룹 정보를 수정하거나 삭제할 수 있다.  

<br><br>

## 구현 내용
### Room 데이터베이스
<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.database.entity.Team

@Entity(
foreignKeys = [ForeignKey(
	entity = Group::class,
	parentColumns = arrayOf("name"),
	childColumns = arrayOf("group_name"),
	onDelete = ForeignKey.CASCADE,
	onUpdate = ForeignKey.CASCADE
)],
indices = [Index("group_name")],
primaryKeys = ["alias", "group_name"]
)

data class Team(
	val name: String,
	@ColumnInfo("group_name") val groupName: String
) {
	var alias = name
	
	var rank: Int = 0
	var win: Int = 0
	var lose: Int = 0
	@ColumnInfo(name = "round_win")
	var roundWin: Int = 0
	@ColumnInfo(name = "round_count")
	var roundCount = 0
	var point: Int = 0
	@ColumnInfo(name = "draw_round")
	var drawRound = 0
	
	constructor(name: String, groupName: String, alias: String) :
					this(name, groupName) {
		this.alias = alias
	}
  
	override fun toString(): String {
		return "${this.alias}: win ${this.win} / round win ${this.roundWin} / point ${this.point}"
	}
}
```

```kotlin
//com.gabin.gongill.database.dao.TeamDao

@Dao
interface TeamDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertTeam(team: Team): Long
		
	@Update
	suspend fun updateTeam(team: Team)		
	
	@Delete
	suspend fun deleteTeam(team: Team)
	
	@Query("SELECT * FROM team WHERE group_name = :groupName")
	suspend fun findByGroup(groupName: String): List<Team>
	
	@Query("SELECT `groups`.*, team.* FROM team JOIN `groups` ON `groups`.name = team.group_name")
	fun getGroupAndTeams(): LiveData<Map<Group, List<Team>>>
	
	// ...
}
```

```kotlin
//com.gabin.gongill.database.MyDatabase

@Database(entities = [Group::class, Team::class, Play::class], version = 3, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class MyDatabase : RoomDatabase() {
	abstract fun groupDao(): GroupDao
	abstract fun teamDao(): TeamDao
	abstract fun playDao(): PlayDao
}
```

```kotlin

class CompetitionRepository(application: Application) {
	private val db = Room.databaseBuilder(
		application.applicationContext, MyDatabase::class.java, "database"
	)
		.addMigrations(MIGRATION_2_3)
		.build()
	
	fun getTeamList(): LiveData<List<Team>> {
		return db.teamDao().getAllTeams()
	}
  
    // ...
}
```
</details>

Room 데이터베이스로 경기, 팀, 그룹 데이터를 저장한다.  
<br>

### ViewModel로 데이터 공유
<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.model.MyViewModel

class MyViewModel(application: Application) : AndroidViewModel(application) {
	private val database by lazy {
		CompetitionRepository(application)
	}
	
	val toastObserver: MutableLiveData<String> = MutableLiveData()
	
	val categoryList: LiveData<List<String>> = database.getCategoryList()
	val groupList: LiveData<List<Group>> = database.getGroupList()
	val teamList: LiveData<List<Team>> = database.getTeamList()
	val playList: LiveData<List<Play>> = database.getPlayList()
	
	val mediator = MediatorLiveData<Unit>()
	
	private var _groupTeamList: MutableLiveData<Map<Group, List<Team>>> = MutableLiveData()
	val groupTeamList: LiveData<Map<Group, List<Team>>>
		get() = _groupTeamList
	
	private var _groupPlayList: MutableLiveData<Map<Group, List<Play>>> = MutableLiveData()
	val groupPlayList: LiveData<Map<Group, List<Play>>>
		get() = _groupPlayList
	
	private var _teamPlayList: MutableLiveData<Map<Team, List<Play>>> = MutableLiveData()
	val teamPlayList: LiveData<Map<Team, List<Play>>>
		get() = _teamPlayList
	
	private var _categoryGroupList: MutableLiveData<Map<String, List<Group>>> = MutableLiveData()
	val categoryGroupList: LiveData<Map<String, List<Group>>>
		get() = _categoryGroupList
	
	
	init {
		mediator.apply {
			addSource(categoryList) {
				updateCategoryGroupList()
				value = Unit
			}
			
			addSource(groupList) {
				updateGroupPlayList()
				updateGroupTeamList()
				updateCategoryGroupList()
				value = Unit
			}
			
			addSource(teamList) {
				updateTeamPlayList()
				updateGroupTeamList()
				updateCurrentTeamAndPlay()
				value = Unit
			}
			
			addSource(playList) {
				updateTeamPlayList()
				updateGroupPlayList()
				updateCurrentTeamAndPlay()
				rank()
				value = Unit
			}
		}
	}
	
	private var selectedCurrentGroup: Group? = null
	var selectedGroup: Group? = null
	var selectedPlay: Play? = null
	var selectedTeam: Team? = null
	
	private var _currentTeamList = MutableLiveData<List<Team>>()
	val currentTeamList: LiveData<List<Team>>
		get() = _currentTeamList
	
	private var _currentPlayList = MutableLiveData<List<Play>>()
	val currentPlayList: LiveData<List<Play>>
		get() = _currentPlayList
	
	
	val changePlayList: ArrayList<Play> = arrayListOf()
	val changeTeamList: ArrayList<Team> = arrayListOf()
  
	fun selectGroup(name: String) {
		selectedCurrentGroup = groupList.value?.find { it.name == name }
		updateCurrentTeamAndPlay()
	}
	
	// ...
}
```
</details>

`ViewModel`로 `Fragment`간 데이터를 공유하였다.  
<br>

### LiveData로 UI 유지
<details>
<summary>코드 보기</summary>

```kotlin
//com.gabin.gongill.view.ui.RankFragment

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {		
	// ...
	
    model.currentTeamList.observe(viewLifecycleOwner) {
        adapter.changeData(it.sortedBy { play -> play.rank })
    }
    
    // ...
}
```
</details>

`LiveData`를 `observe`하여 변경되는 데이터를 UI에 반영하고, 유지되도록 했다.

<br><br>
