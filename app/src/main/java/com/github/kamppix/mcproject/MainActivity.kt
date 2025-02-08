package com.github.kamppix.mcproject

import SampleData
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            InitProfileName()
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
fun InitProfileName() {
    val context = LocalContext.current
    val nameFile = File(context.filesDir, "profileName")
    if (!nameFile.exists()) nameFile.writeBytes("User".toByteArray())
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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable<ChatDest> {
            ChatScreen(
                SampleData.conversationSample,
                onNavigateToSettings = {
                    navController.navigate(route = SettingsDest)
                }
            )
        }
        composable<SettingsDest> {
            SettingsScreen(
                onNavigateToChat = {
                    navController.navigate(route = ChatDest) {
                        popUpTo(ChatDest) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    MCProjectTheme {
        Surface {
            MessageCard(
                msg = Message("Kamppi", "Hey, take a look at Jetpack Compose, it's great!")
            )
        }
    }
}

@Preview
@Composable
fun PreviewChat() {
    MCProjectTheme {
        Surface {
            Chat(
                SampleData.conversationSample
            )
        }
    }
}

@Preview
@Composable
fun PreviewProfileSettings() {
    MCProjectTheme {
        Surface {
            ProfileSettings()
        }
    }
}
