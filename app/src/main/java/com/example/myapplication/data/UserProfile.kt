package com.example.myapplication.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,  // Single row, always ID = 1
    val name: String,
    val imagePath: String
)