package com.example.tokomurahinventory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.database.Converters
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.UsersTable

@Database(entities = [MerkTable::class,WarnaTable::class,DetailWarnaTable::class,UsersTable::class,LogTable::class,BarangLog::class],version=3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class DatabaseInventory: RoomDatabase()  {
    abstract val merkDao :MerkDao
    abstract val warnaDao:WarnaDao
    abstract val detailWarnaDao:DetailWarnaDao
    abstract val usersDao : UsersDao
    abstract val logDao:LogDao
    abstract val barangLogDao:BarangLogDao

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