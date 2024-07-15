package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.UsersTable

@Dao
interface UsersDao {
    @Insert
    fun insert(usersTable: UsersTable)

    @Update
    fun update(usersTable: UsersTable)

    @Query("DELETE FROM users_table WHERE id=:id")
    fun deleteAnItemUser(id:Int)

    @Query("SELECT * FROM users_table")
    fun selectAllUsers():LiveData<List<UsersTable>>

    @Query("SELECT * FROM users_table")
    fun selectAllUsersList():List<UsersTable>

    @Query("SELECT COUNT(*) FROM users_table WHERE userName = :userName AND password = :password")
    fun getUser(userName: String, password: String): Int

    @Query("SELECT COUNT(*) FROM users_table")
    fun getUserCount(): Int
    @Insert
    suspend fun insertUser(user: UsersTable)

    @Query("SELECT COUNT(*) FROM users_table WHERE userName = :username")
    fun checkUserExists(username: String): Int

    @Query("SELECT * FROM users_table WHERE userName = :username")
    suspend fun getUserByUsername(username: String): UsersTable?
}