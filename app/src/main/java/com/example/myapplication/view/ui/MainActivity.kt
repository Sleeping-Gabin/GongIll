package com.example.myapplication.view.ui

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.node.getOrAddAdapter
import androidx.core.content.edit
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.view.model.MyViewModel
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var pref: SharedPreferences
    private val model: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = getSharedPreferences("previous_state", MODE_PRIVATE)

        initNavController()
        //initSelectGroupText()

        model.toastObserver.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        model.teamList.observe(this) {
            model.updateGroupTeamList()
            model.updateTeamPlayList()
            model.updateCurrentTeamAndPlay()
        }

        model.playList.observe(this) {
            model.updateTeamPlayList()
            model.updateGroupPlayList()
            model.updateCurrentTeamAndPlay()
        }

        model.categoryList.observe(this) {
            model.updateCategoryGroupList()
        }

        model.groupList.observe(this) {
            model.updateGroupPlayList()
            model.updateGroupTeamList()
            model.updateCategoryGroupList()
        }

    }

    private fun initSelectGroupText() {
        val arrayAdapter = ArrayAdapter(this, R.layout.group_array_item, arrayListOf(""))
        binding.selectGroupText.setAdapter(arrayAdapter)
        binding.selectGroupText.setText("", false)
        binding.selectGroupText.setOnItemClickListener { adapterView, view, position, id ->
            val groupName = adapterView.getItemAtPosition(position).toString()
            changeSelectGroupText(groupName)
        }

        model.groupList.observe(this) {
            val list = it.map { g -> if (g.category != "others") g.category + " - " + g.name else g.name}
            val currentText = binding.selectGroupText.text.toString()

            arrayAdapter.clear()
            arrayAdapter.addAll(list)

            if (list.isEmpty()) {
                changeSelectGroupText("")
            }
            else if (currentText == "") {
                val selectedGroup = pref.getString("group", "")
                if (list.contains(selectedGroup)) {
                    changeSelectGroupText(selectedGroup ?: "")
                }
                else {
                    changeSelectGroupText(list[0])
                }
            }
            else if (!list.contains(currentText)){
                changeSelectGroupText(list[0])
            }
        }
    }

    private fun changeSelectGroupText(newText: String) {
        val txt = newText.split(" - ")
        binding.selectGroupText.setText(newText, false)
        model.selectGroup(txt.last())
        pref.edit {
            putString("group", newText)
        }
    }

    private fun initNavController() {
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.scheduleFragment, R.id.groupFragment, R.id.rankFragment))
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.hostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        //binding.navBar.setupWithNavController(navController)
        NavigationUI.setupWithNavController(binding.navBar, navController, false)
        navController.addOnDestinationChangedListener { controller, destination, argument ->
            when(destination.id) {
                R.id.scheduleFragment -> {
                    binding.selectGroup.visibility = View.VISIBLE
                    binding.navBar.visibility = View.VISIBLE
                    binding.appBar.visibility = View.VISIBLE
                    binding.toolBar.setTitle(R.string.title_schedule)
                }
                R.id.rankFragment -> {
                    binding.selectGroup.visibility = View.VISIBLE
                    binding.navBar.visibility = View.VISIBLE
                    binding.appBar.visibility = View.VISIBLE
                    binding.toolBar.setTitle(R.string.title_team)
                }
                R.id.groupFragment -> {
                    binding.selectGroup.visibility = View.GONE
                    binding.navBar.visibility = View.VISIBLE
                    binding.appBar.visibility = View.VISIBLE
                    binding.toolBar.setTitle(R.string.title_group)
                }
                R.id.predictResultFragment -> {
                    binding.navBar.visibility = View.VISIBLE
                    binding.appBar.visibility = View.VISIBLE
                    binding.toolBar.setTitle(R.string.title_result)
                    binding.toolBar.navigationIcon = AppCompatResources.getDrawable(
                        applicationContext, R.drawable.arrow_back_white)
                }
                else -> {
                    binding.selectGroup.visibility = View.GONE
                    binding.navBar.visibility = View.GONE
                    binding.appBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        initSelectGroupText()
        super.onResume()
    }
}