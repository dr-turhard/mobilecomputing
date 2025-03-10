package com.example.myapplication.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.user.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.example.myapplication.data.user.UserDao



fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    val file = File(context.filesDir, "profile_picture.jpg")
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(file)

    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}
// Update Profile in Database
fun updateProfile(userDao: UserDao, name: String, imagePath: String?, scope: CoroutineScope) {
    scope.launch {
        val existingProfile = userDao.getUserProfile()
        if (existingProfile != null) {
            userDao.updateUserProfile(UserProfile(1, name, imagePath ?: ""))
        } else {
            userDao.insertUserProfile(UserProfile(1, name, imagePath ?: ""))
        }
    }
}

@Composable
fun ProfileView(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()

    var textInput by remember { mutableStateOf("User") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val profile = userDao.getUserProfile()
            if (profile != null) {
                textInput = profile.name
                imagePath = profile.imagePath
            }
        }
    }

    val photoFile = remember {
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_pic.jpg")
    }

    val photoUri: Uri = FileProvider.getUriForFile(
        context, "${context.packageName}.provider", photoFile
    )

    //Pick image from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(context, uri)
            imagePath = savedPath
            updateProfile(userDao, textInput, savedPath, scope)
        }
    }


    //Camera functionality. This was easier and works
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val savedPath = photoFile.absolutePath
            imagePath = savedPath

            scope.launch {
                updateProfile(userDao, textInput, savedPath, scope)
            }
        }
    }






    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(text = "Edit Profile", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = {
                    textInput = it
                    scope.launch { val existingProfile = userDao.getUserProfile()
                        if (existingProfile != null) {
                            userDao.updateUserProfile(UserProfile(1, it, imagePath ?: ""))
                        } else {
                            userDao.insertUserProfile(UserProfile(1, it, imagePath ?: ""))
                        }
                    }
                },
                label = { Text("Your Name") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            imagePath?.let {
                AsyncImage(
                    model = File(it).absolutePath + "?time=" + System.currentTimeMillis(),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Pick an Image from Gallery")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { cameraLauncher.launch(photoUri) }) { //Opens the camera application
                Text("Take a Picture")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Save & Go Back")
            }
        }
    }
}