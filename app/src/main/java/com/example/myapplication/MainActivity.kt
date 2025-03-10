package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import com.example.myapplication.screens.MenuView
import com.example.myapplication.screens.ConversationView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.myapplication.screens.ProfileView
import kotlinx.coroutines.launch
import java.io.File
import com.example.myapplication.notifications.Notifications
import com.example.myapplication.notifications.SensorListener
import com.example.myapplication.data.message.Message


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Ask for permission to send notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
                )
            }
        }
        Notifications.createNotificationChannel(this)
        SensorListener(this)
        setContent {
            MaterialTheme {
                Main()
                }
            }
        }
    }


@Composable
fun Main(){
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStoreManager = remember { DataStoreManager(context) }

    var userName by remember { mutableStateOf("User") }
    var userImageUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            dataStoreManager.userText.collect { storedName ->
                if (storedName != null) userName = storedName
            }
            dataStoreManager.userImage.collect { storedUri ->
                val file = File(context.filesDir, "profile_picture.jpg")
                userImageUri = if (file.exists()) file.toURI().toString() else storedUri
            }
        }
    }


    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "menu",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("menu") { MenuView(navController) }
            composable("profile") { ProfileView(navController) }
            composable("conversation") { ConversationView(navController) }
        }
    }
    /*  This was used to display a notification button for testing
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Button(onClick = { Notifications.triggerNotification(context) }) {
            Text("Trigger Notification")
        }
    }

     */
}

//data class Message(val author: String, val body: String, val imageUri: String?)

@Composable
fun MessageCard(msg: com.example.myapplication.data.message.Message){
    Row(modifier = Modifier.padding(all = 8.dp)) {
        if(msg.imageUri != null){
            AsyncImage(
                model = msg.imageUri,
                contentDescription = "Profile picture",
                modifier = Modifier
                    // set image size
                    .size(40.dp)
                    // clip image to circle shape
                    .clip(CircleShape)
                    .border(1.6.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else{
            Image(
                painter = painterResource(R.drawable.catstare),
                contentDescription = "Default profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.6.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Keeping track if message is expanded or not
        var isExpanded by remember { mutableStateOf(false) }
        // surfaceColor gradual update from one to another
        val surfaceColor by animateColorAsState(
            if(isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.sender,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
            // add vertical space between author and message
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape =  MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                //surfaceColor changing gradually from primary to surface
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.content,
                    modifier = Modifier.padding(all = 4.dp),
                    // Display all content is the message is expanded
                    maxLines = if(isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}



@Composable
fun Conversation(messages: List<Message>){
    LazyColumn {
        items(messages){message ->
            MessageCard(message)
        }
    }
}
/*@Preview
@Composable
fun PreviewConversation() {
    MaterialTheme {
        Conversation(SampleData.conversationSample)
    }
}
 */
/*
@Preview(name ="Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    )

 */
/*
@Composable
fun PreviewMessageCard() {
    MyApplicationTheme {
        Surface {
            MessageCard(
                msg = Message("Tuukka", "Hey, take a look at Jetpack Compose, it's great!")
            )
        }
    }
}
*/



