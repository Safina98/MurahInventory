package com.example.tokomurahinventory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tokomurahinventory.models.MerkTable

@Database(entities = [MerkTable::class],version=1, exportSchema = true)
abstract class DatabaseInventory: RoomDatabase()  {
    abstract val merkDao :MerkDao

    companion object{
        @Volatile
        private var INSTANCE: DatabaseInventory?=null
        fun getInstance(context: Context):DatabaseInventory{
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DatabaseInventory::class.java,
                        "inventory_table"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    //instance = Room.databaseBuilder(context.applicationContext,VendibleDatabase::class.java,"mymaindb").allowMainThreadQueries().build()
                }
                return instance
            }
        }
    }
}