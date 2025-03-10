package com.example.myapplication.data.message

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val content: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)