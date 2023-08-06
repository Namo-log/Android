package com.example.namo.data.dao

import androidx.room.*
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event

@Dao
interface CategoryDao {

    @Insert
    fun insertCategory(category: Category) : Long

    @Update
    fun updateCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)

    @Query("SELECT * FROM category_table")
    fun getCategoryList(): List<Category>

    @Query("SELECT COUNT(*) FROM category_table")
    fun getAllCategorySize() : Int

    @Query("SELECT * FROM category_table WHERE active=:isActive")
    fun getActiveCategoryList(isActive: Boolean): List<Category>

    @Query("SELECT * FROM category_table WHERE categoryIdx=:categoryIdx UNION ALL SELECT * FROM category_table WHERE categoryIdx <> :categoryIdx LIMIT 1")
    fun getCategoryWithId(categoryIdx: Long): Category

    @Query("SELECT * FROM category_table WHERE isUpload = 0")
    fun getNotUploadedCategory() : List<Category>

    @Query("UPDATE category_table SET isUpload=:isUpload, serverIdx=:serverIdx, state=:state WHERE categoryIdx=:categoryIdx")
    fun updateCategoryAfterUpload(categoryIdx : Long, isUpload : Int, serverIdx : Long, state : String)

}
