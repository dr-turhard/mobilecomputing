package com.example.myapplication

object SampleData {
    fun getConversationSample(userName: String, userImageUri: String?): List<Message> {
        return listOf(
            Message(userName, "Test...Test...Test...", userImageUri),
            Message(userName, """List of Android versions:
                |Android KitKat (API 19)
                |Android Lollipop (API 21)
                |Android Marshmallow (API 23)
                |Android Nougat (API 24)
                |Android Oreo (API 26)
                |Android Pie (API 28)
                |Android 10 (API 29)
                |Android 11 (API 30)
                |Android 12 (API 31)""".trim(), userImageUri),
            Message(userName, "I think Kotlin is my favorite programming language.", userImageUri),
            Message(userName, "Searching for alternatives to XML layouts...", userImageUri),
            Message(userName, """Hey, take a look at Jetpack Compose, it's great!
                |It's the Android's modern toolkit for building native UI.
                |It simplifies and accelerates UI development on Android.
                |Less code, powerful tools, and intuitive Kotlin APIs :)""".trim(), userImageUri),
            Message(userName, "It's available from API 21+ :)", userImageUri),
            Message(userName, "Writing Kotlin for UI seems so natural, Compose where have you been all my life?", userImageUri),
            Message(userName, "Android Studio next version's name is Arctic Fox", userImageUri),
            Message(userName, "Android Studio Arctic Fox tooling for Compose is top notch ^_^", userImageUri),
            Message(userName, "I didn't know you can now run the emulator directly from Android Studio", userImageUri),
            Message(userName, "Compose Previews are great to check quickly how a composable layout looks like", userImageUri),
            Message(userName, "Previews are also interactive after enabling the experimental setting", userImageUri),
            Message(userName, "Have you tried writing build.gradle with KTS?", userImageUri)
        )
    }
}
