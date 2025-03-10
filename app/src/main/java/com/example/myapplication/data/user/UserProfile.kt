package com.example.myapplication.data.user
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,  // Single row, ID always one now
    val name: String,
    val imagePath: String
)