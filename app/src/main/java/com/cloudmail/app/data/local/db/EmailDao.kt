package com.cloudmail.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EmailEntity>)

    @Query("SELECT * FROM emails WHERE accountId = :accountId AND type = :type ORDER BY emailId DESC LIMIT :limit")
    fun observeRecent(accountId: Long, type: Int, limit: Int = 100): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE emailId = :emailId LIMIT 1")
    suspend fun getById(emailId: Long): EmailEntity?

    @Query("DELETE FROM emails WHERE accountId = :accountId")
    suspend fun clearByAccount(accountId: Long)

    @Query("UPDATE emails SET unread = :unread WHERE emailId IN (:ids)")
    suspend fun updateUnread(ids: List<Long>, unread: Int)

    @Query("UPDATE emails SET isStar = :star WHERE emailId IN (:ids)")
    suspend fun updateStar(ids: List<Long>, star: Int)

    @Query("DELETE FROM emails WHERE emailId IN (:ids)")
    suspend fun markDeleted(ids: List<Long>)
}
