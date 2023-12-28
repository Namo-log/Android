package com.mongmong.namo.data.dao

import androidx.room.*
import com.mongmong.namo.data.entity.group.Group

@Dao
interface GroupDao {
    @Insert
    fun insertGroup(group: Group)

    @Update
    fun updateGroup(group: Group)

    @Delete
    fun deleteGroup(group: Group)

    @Query("SELECT * FROM group_table")
    fun getGroupList() : List<Group>

    @Query("SELECT * FROM group_table WHERE groupIdx=:idx")
    fun getGroup(idx:Int): Group
}