package com.mongmong.namo.data.local.dao

import androidx.room.*
import com.mongmong.namo.data.local.entity.home.Category

@Dao
interface CategoryDao {

    @Insert
    fun insertCategory(category: Category) : Long

    @Update
    fun updateCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)

    @Query("DELETE FROM category_table")
    fun deleteAllCategories()

    @Query("DELETE FROM category_table WHERE categoryId=:categoryId")
    fun deleteCategoryById(categoryId : Long)


    @Query("SELECT * FROM category_table")
    fun getCategoryList(): List<Category>

    @Query("SELECT COUNT(*) FROM category_table")
    fun getAllCategorySize() : Int

    @Query("SELECT * FROM category_table WHERE active=:isActive")
    fun getActiveCategoryList(isActive: Boolean): List<Category>

    @Query("SELECT * FROM category_table WHERE categoryId=:categoryId UNION ALL SELECT * FROM category_table WHERE categoryId <> :categoryId LIMIT 1")
    fun getCategoryWithId(categoryId: Long): Category

    @Query("SELECT * FROM category_table WHERE isUpload = 0")
    fun getNotUploadedCategory() : List<Category>

    @Query("UPDATE category_table SET isUpload=:isUpload, serverId=:serverId, state=:state WHERE categoryId=:categoryId")
    fun updateCategoryAfterUpload(categoryId : Long, isUpload : Int, serverId : Long, state : String)

}
