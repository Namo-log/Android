package com.example.namo.data.dao

import androidx.room.*
import com.example.namo.ui.bottom.home.schedule.data.Category

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

    @Query("SELECT * FROM category_table WHERE categoryIdx=:categoryIdx")
    fun getCategoryContent(categoryIdx: Int): Category

}
