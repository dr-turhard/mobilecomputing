package com.example.myapplication.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView


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

fun startCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    executor: ExecutorService,
    imageCapture: ImageCapture
): PreviewView {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val previewView = PreviewView(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageCapture
            )
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))

    return previewView
}

@Composable
fun ProfileView(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()

    var textInput by remember { mutableStateOf("User") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    //This initializes the camera executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

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

    val previewView = remember { startCamera(context, lifecycleOwner, cameraExecutor, imageCapture) }

    //Pick image from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(context, uri)
            imagePath = savedPath
            updateProfile(userDao, textInput, savedPath, scope)
            /*scope.launch {
                val existingProfile = userDao.getUserProfile()
                if (existingProfile != null) {
                    userDao.updateUserProfile(UserProfile(1, textInput, savedPath))
                } else {
                    userDao.insertUserProfile(UserProfile(1, textInput, savedPath))
                }
                imagePath = savedPath


            }

             */
        }
    }

    //Camera functionalilty
    /*
    fun takePicture(
        context: Context,
        userDao: UserDao,
        textInput: String,
        scope: CoroutineScope,
        setImagePath: (String) -> Unit
    ) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_pic_${System.currentTimeMillis()}.jpg"
        )
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    imageCapture
                )

                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedPath = file.absolutePath
                            Log.d("Camera", "Image saved at: $savedPath")

                           //Should update UI(?)
                            setImagePath(savedPath)

                            scope.launch {
                                updateProfile(userDao, textInput, savedPath, scope)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Camera", "Image capture failed: ${exception.message}")
                        }
                    }
                )
            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

     */

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

    fun takePicture(
        context: Context,
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        executor: ExecutorService,
        imageCapture: ImageCapture,
        userDao: UserDao,
        textInput: String,
        scope: CoroutineScope,
        setImagePath: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                val imageCaptureUseCase = ImageCapture.Builder().build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, imageCaptureUseCase
                )

                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "profile_pic_${System.currentTimeMillis()}.jpg"
                )

                val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                imageCaptureUseCase.takePicture(outputFileOptions, executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedPath = file.absolutePath
                            Log.d("Camera", "Image saved at: $savedPath")

                            if (file.exists()) {
                                setImagePath(savedPath)

                                scope.launch {
                                    updateProfile(userDao, textInput, savedPath, scope)
                                }
                            } else {
                                Log.e("Camera", "Image file does not exist after capture")
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Camera", "Image capture failed: ${exception.message}")
                        }
                    }
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
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

            /*
            Button(onClick = {
                takePicture(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    executor = cameraExecutor,
                    imageCapture = imageCapture,
                    userDao = userDao,
                    textInput = textInput,
                    scope = scope
                ) { newPath ->
                    imagePath = newPath //updated image path, allows to take more than one picture
                }
            }) {
                Text("Take a Picture")
            }

             */

            Button(onClick = { cameraLauncher.launch(photoUri) }) { // ✅ Opens Camera App
                Text("Take a Picture")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Save & Go Back")
            }
        }
    }
}