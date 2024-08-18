package com.example.tokomurahinventory.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns with TEXT type for Date
            database.execSQL("""
            ALTER TABLE detail_warna_table
            ADD COLUMN dateIn TEXT;
        """)
            database.execSQL("""
            ALTER TABLE detail_warna_table
            ADD COLUMN dateOut TEXT;
        """)

            // Populate the new columns with the value from detailWarnaDate
            database.execSQL("""
            UPDATE detail_warna_table
            SET dateIn = detailWarnaDate,
                dateOut = detailWarnaDate;
        """)
        }
    }

}
