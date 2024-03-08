package com.mongmong.namo.data.local.dao

import androidx.room.*
import com.mongmong.namo.data.local.entity.group.Group

@Dao
interface GroupDao {
    @Insert
    fun insertGroup(group: Group)

    @Update
    fun updateGroup(group: Group)

    @Delete
    fun deleteGroup(group: Group)

    @Query("DELETE FROM group_table")
    fun deleteAllGroups()

    @Query("SELECT * FROM group_table")
    fun getGroupList() : List<Group>

    @Query("SELECT * FROM group_table WHERE groupIdx=:idx")
    fun getGroup(idx:Int): Group
}