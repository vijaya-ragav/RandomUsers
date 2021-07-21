package com.ragavan.randomusers.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ragavan.randomusers.model.Results

@Dao
interface RandomUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(result: List<Results>)

    @Query("SELECT * FROM Results WHERE first LIKE :search || '%'")
    suspend fun getSearchResult(search: String): List<Results>

    @Query("SELECT * FROM Results WHERE email LIKE :search || '%'")
    suspend fun getUserDetails(search: String): Results

    @Query("SELECT COUNT(*) FROM Results")
    suspend fun getCount():Int

    @Query("SELECT * FROM Results")
    fun getAllUsers(): PagingSource<Int, Results>

    @Query("DELETE FROM Results")
    suspend fun clearAllUsers()
}