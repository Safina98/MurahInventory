package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tokomurahinventory.models.UsersTable

@Dao
interface UsersDao {
    @Insert
    fun insert(usersTable: UsersTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsersTable(user: UsersTable)

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

    @Query("SELECT * FROM users_table WHERE userName = :username COLLATE NOCASE")
    suspend fun getUserByUsername(username: String): UsersTable?

    @Query("SELECT * FROM users_table WHERE userName = :username AND password = :password COLLATE NOCASE")
    suspend fun getUserByUserNameAndPassword(username: String,password: String): UsersTable?

    /////////////////////////Others table
    @Query("UPDATE merk_table SET createdBy = :defaultUser WHERE createdBy IS NULL")
    fun updateCreatedByForDeletedUser(defaultUser: String)


    @Query("UPDATE merk_table SET lastEditedBy = :defaultUser WHERE lastEditedBy IS NULL")
    fun updateLastEditedByForDeletedUser(defaultUser: String)

    @Query("SELECT COUNT(*) FROM users_table WHERE userRole = :role")
    fun countAdmins(role: String): Int
    @Transaction
    fun deleteUserWithReferences(user: UsersTable, defaultUser: String) {
        deleteAnItemUser(user.id)
        // Update references in merk_table
        //updateCreatedByForDeletedUser( defaultUser)
        //updateLastEditedByForDeletedUser( defaultUser)
        // Delete the user

    }
}