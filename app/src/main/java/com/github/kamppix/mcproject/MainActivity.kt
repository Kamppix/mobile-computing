package com.github.kamppix.mcproject

import SampleData
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.kamppix.mcproject.ui.theme.MCProjectTheme
import kotlinx.serialization.Serializable
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MCProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppLayout(
    topBarContentLeft: @Composable () -> Unit = {},
    topBarContentRight: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    Spacer(Modifier.width(4.dp))
                    topBarContentLeft()
                }
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    topBarContentRight()
                    Spacer(Modifier.width(4.dp))
                }
            }
        }
        content()
    }
}

@Composable
fun VerticalCenter(
    item: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item()
    }
}

@Serializable
object ChatDest
@Serializable
object SettingsDest

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = ChatDest
    ) {
    val context = LocalContext.current

    // Init name
    val nameFile = File(context.filesDir, "profileName")
    if (!nameFile.exists()) nameFile.writeBytes("User".toByteArray())
    var profileName by remember { mutableStateOf(nameFile.readBytes().decodeToString()) }

    // Init picture
    val pictureUriFile = File(context.filesDir, "profilePictureUri")
    if (!pictureUriFile.exists()) pictureUriFile.writeBytes("".toByteArray())
    var profilePicture by remember { mutableStateOf(pictureUriFile.readBytes().decodeToString()) }

    val pfpPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Delete old picture if exists
            val oldFile = File(Uri.parse(profilePicture).path ?: "")
            if (oldFile.exists()) oldFile.delete()

            // Copy picked image to new file location
            val resolver = context.contentResolver
            val newFile = File(context.filesDir, "pfp-${System.currentTimeMillis()}")
            resolver.openInputStream(uri).use { stream ->
                stream?.copyTo(newFile.outputStream())
            }

            // Write new picture URI to file
            pictureUriFile.writeBytes(newFile.toUri().toString().toByteArray())
            profilePicture = pictureUriFile.readBytes().decodeToString()
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable<ChatDest> {
            ChatScreen(
                SampleData.conversationSample,
                profileName,
                profilePicture,
                onNavigateToSettings = {
                    navController.navigate(route = SettingsDest)
                }
            )
        }
        composable<SettingsDest> {
            SettingsScreen(
                profileName,
                profilePicture,
                onChangeProfileName = { newName ->
                    nameFile.writeBytes(newName.toByteArray())
                    profileName = nameFile.readBytes().decodeToString()
                },
                onChangeProfilePicture = {
                    pfpPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onNavigateBack = {
                    navController.navigate(route = ChatDest) {
                        popUpTo(ChatDest) { inclusive = true }
                    }
                }
            )
        }
    }
}
