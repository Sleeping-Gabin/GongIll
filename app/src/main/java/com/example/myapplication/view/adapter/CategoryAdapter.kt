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

/**
 * 카테고리 목록을 보여주는 Adapter
 *
 * @param[map] 카테고리별 그룹 목록
 * @param[listener] [GroupAdapter]에서 아이템을 터치했을 때 호출되는 [OnGroupTouchListener]
 * @param[model] 앱의 데이터를 공유하는 [MyViewModel] 객체
 *
 * @property[categoryGroups] 카테고리별 그룹 목록
 * @property[categoryList] 카테고리 목록.
 * 카테고리 미지정 시의 "others"가 첫번째 요소
 */
class CategoryAdapter(map: Map<String, List<Group>>?, val listener: OnGroupTouchListener, val model: MyViewModel): RecyclerView.Adapter<ViewHolder>() {
    var categoryGroups = map?.toMap() ?: mapOf()
    var categoryList = listOf("others") + categoryGroups.keys.filterNot { it == "others" }

    /**
     * 카테고리 아이템의 ViewHolder
     *
     * @param[binding] CategoryItemBinding 객체
     */
    class ViewHolder(val binding:CategoryItemBinding): RecyclerView.ViewHolder(binding.root)

    /**
     * ViewHolder가 생성될 때 호출
     *
     * 카테고리 이름 클릭 이벤트의 listener를 추가한다.
     *
     * @param[parent] 부모 ViewGroup 객체
     * @param[viewType] view의 타입
     *
     * @return 생성된 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        //누를 때마다 카테고리를 확장/축소
        binding.categoryTitle.setOnClickListener {
            if (binding.groupView.visibility == View.VISIBLE) { //축소
                binding.groupView.visibility = View.GONE
                binding.expandBtn.setImageResource(R.drawable.expand_more)
            }
            else { //확장
                binding.groupView.visibility = View.VISIBLE
                binding.expandBtn.setImageResource(R.drawable.expand_less)
            }
        }

        binding.groupView.layoutManager = LinearLayoutManager(parent.context, RecyclerView.HORIZONTAL, false)

        return ViewHolder(binding)
    }

    /**
     * ViewHolder가 연결될 때 호출
     *
     * 카테고리의 그룹 목록을 표시한다.
     * 카테고리가 없는 그룹은 카테고리 이름 없이 목록을 바로 표시한다.
     *
     * @param[holder] 연결할 ViewHolder 객체
     * @param[position] ViewHolder의 위치
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val category = categoryList[position]
        val binding = (holder as ViewHolder).binding

        //카테고리에 그룹 표시
        val adapter = GroupAdapter(category, categoryGroups[category], listener, model)
        binding.groupView.adapter = adapter

        binding.categoryTitleText.text = category

        //카테고리가 없는 그룹은 카테고리 이름을 표시하지 않고 무조건 그룹 목록 표시
        if (category == "others") {
            binding.categoryTitle.visibility = View.GONE
            binding.groupView.visibility = View.VISIBLE
        }
    }

    /**
     * ViewHolder의 개수인 [categoryList]의 크기를 반환한다.
     *
     * @return ViewHolder의 개수
     */
    override fun getItemCount(): Int {
        return categoryList.size
    }

    /**
     * 연결된 데이터를 변경한다.
     *
     * @param[newMap] 변경할 데이터
     */
    fun changeData(newMap: Map<String, List<Group>>?) {
        categoryGroups = newMap ?: mapOf()
        categoryList = listOf("others") + categoryGroups.keys.filterNot { it == "others" }
        notifyDataSetChanged()
    }
}