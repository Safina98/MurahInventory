package com.example.tokomurahinventory.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.models.WarnaTable
import java.util.concurrent.Executors


@Database(entities = [MerkTable::class,WarnaTable::class,DetailWarnaTable::class,UsersTable::class,LogTable::class,BarangLog::class],version=1, exportSchema = true)
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
                        "inventory_table.db"
                    ) .addCallback(DatabaseCallback())
                        .fallbackToDestructiveMigration()
                        .setQueryExecutor(Executors.newSingleThreadExecutor()) // To handle SQL queries
                        .setTransactionExecutor(Executors.newSingleThreadExecutor()) // To handle transactions
                        .build()
                    INSTANCE = instance
                    //instance = Room.databaseBuilder(context.applicationContext,VendibleDatabase::class.java,"mymaindb").allowMainThreadQueries().build()
                }
                return instance
            }
        }
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Enable SQL logging
                db.execSQL("PRAGMA foreign_keys=ON")
                db.query("PRAGMA foreign_keys")
                db.enableWriteAheadLogging()
            }
        }
    }
}