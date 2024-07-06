package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users_table",
    indices = [Index(value = ["userRef"], unique = true,
        ),Index(value = ["userName"], unique = true,
    )])
data class UsersTable(
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    @ColumnInfo(name="userName")
    var userName:String="",
    @ColumnInfo(name="password")
    var password:String="",
    @ColumnInfo(name="userRef")
    var usersRef:String=""
)