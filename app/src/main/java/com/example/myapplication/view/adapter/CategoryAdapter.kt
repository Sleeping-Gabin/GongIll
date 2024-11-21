package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.myapplication.R
import com.example.myapplication.database.entity.Group
import com.example.myapplication.databinding.CategoryItemBinding
import com.example.myapplication.view.listener.OnGroupTouchListener
import com.example.myapplication.view.model.MyViewModel

class CategoryAdapter(map: Map<String, List<Group>>?, val listener: OnGroupTouchListener, val model: MyViewModel): RecyclerView.Adapter<ViewHolder>() {
    var categoryGroups = map?.toMap() ?: mapOf()
    var categoryList = listOf("others") + categoryGroups.keys.filterNot { it == "others" }

    class ViewHolder(val binding:CategoryItemBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.categoryTitle.setOnClickListener {
            if (binding.groupView.visibility == View.VISIBLE) {
                binding.groupView.visibility = View.GONE
                binding.expandBtn.setImageResource(R.drawable.expand_more)
            }
            else {
                binding.groupView.visibility = View.VISIBLE
                binding.expandBtn.setImageResource(R.drawable.expand_less)
            }
        }
        binding.groupView.layoutManager = LinearLayoutManager(parent.context, RecyclerView.HORIZONTAL, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val category = categoryList[position]
        val binding = (holder as ViewHolder).binding

        val adapter = GroupAdapter(category, categoryGroups[category], listener, model)
        binding.groupView.adapter = adapter

        binding.categoryTitleText.text = category

        if (category == "others") {
            binding.categoryTitle.visibility = View.GONE
            binding.groupView.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun changeData(newMap: Map<String, List<Group>>?) {
        categoryGroups = newMap ?: mapOf()
        categoryList = listOf("others") + categoryGroups.keys.filterNot { it == "others" }
        notifyDataSetChanged()
    }
}