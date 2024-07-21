package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.example.tokomurahinventory.database.Converters


@Entity(
    tableName = "detail_warna_table",
    indices = [
        Index(value = ["detailWarnaRef"], unique = true),
        Index(value = ["createdBy"]),
        Index(value = ["lastEditedBy"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = WarnaTable::class,
            parentColumns = ["warnaRef"],
            childColumns = ["warnaRef"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsersTable::class,
            parentColumns = ["userName"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsersTable::class,
            parentColumns = ["userName"],
            childColumns = ["lastEditedBy"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class DetailWarnaTable(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "warnaRef")
    var warnaRef: String = "",
    @ColumnInfo(name = "detailWarnaIsi")
    var detailWarnaIsi: Double = 0.0,
    @ColumnInfo(name = "detailWarnaPcs")
    var detailWarnaPcs: Int = 0,
    @ColumnInfo(name = "detailWarnaRef")
    var detailWarnaRef: String = "",
    @ColumnInfo(name = "detailWarnaDate")
    var detailWarnaDate: Date = Date(),
    @ColumnInfo(name = "detailWarnaLastEditedDate")
    var detailWarnaLastEditedDate: Date = Date(),
    @ColumnInfo(name = "user")
    var user: String? = "",
    @ColumnInfo(name = "createdBy")
    var createdBy: String? = "",
    @ColumnInfo(name = "lastEditedBy")
    var lastEditedBy: String? = ""
)
