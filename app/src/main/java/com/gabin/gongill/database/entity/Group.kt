package com.gabin.gongill.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gabin.gongill.objects.RankRule

/**
 * 경기 데이터 클래스
 *
 * @param[name] 그룹 이름
 * @param[playNum] 같은 팀과의 경기 횟수
 * @param[rule] 경기 순위 매기는 규칙
 *
 * @property[category] 그룹의 카테고리. 미지정 시 "others"
 *
 * @constructor 새로운 그룹을 생성. 카테고리 미지정 시 "others"로 설정
 */
@Entity(
	tableName = "groups",
)
data class Group(
	@PrimaryKey val name: String,
	var playNum: Int = 1,
	var rule: RankRule
) {
	var category: String = "others"
	
	constructor(category: String, name: String, playNum: Int, rule: RankRule) :
					this(name, playNum, rule) {
		this.category = category
	}
}