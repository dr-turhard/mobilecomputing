package com.example.myapplication
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class DataStoreManager(private val context: Context) {
    companion object{
        val TEXT_KEY = stringPreferencesKey("user_text")
        val IMAGE_KEY = stringPreferencesKey("user_image")
    }
    val userText: Flow<String?> = context.dataStore.data.map { preferences -> preferences[TEXT_KEY] }

    val userImage: Flow<String?> = context.dataStore.data.map { preferences -> preferences[IMAGE_KEY] }

    suspend fun saveUserText(text: String){
        context.dataStore.edit { preferences -> preferences[TEXT_KEY] = text }
    }

    suspend fun saveUserImage(uri: String){
        context.dataStore.edit { preferences -> preferences[IMAGE_KEY] = uri }
    }

}