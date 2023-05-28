package com.example.namo.data.dao

import androidx.room.*
import com.example.namo.data.entity.home.Category

@Dao
interface CategoryDao {

    @Insert
    fun insertCategory(category: Category)

    @Update
    fun updateCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)

    @Query("SELECT * FROM category_table")
    fun getCategoryList(): List<Category>

    @Query("SELECT * FROM category_table WHERE categoryIdx=:categoryIdx UNION ALL SELECT * FROM category_table WHERE categoryIdx <> :categoryIdx LIMIT 1")
    fun getCategoryContent(categoryIdx: Int): Category

}
