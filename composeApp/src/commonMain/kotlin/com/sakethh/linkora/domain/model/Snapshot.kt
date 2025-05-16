package com.sakethh.linkora.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snapshot")
data class Snapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val content: String
)
