package com.github.kamppix.mcproject

import SampleData
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.kamppix.mcproject.ui.theme.MCProjectTheme
import kotlinx.serialization.Serializable

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

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 12.dp)) {
        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = "User profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "Expansion color animation"
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                shadowElevation = 3.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Chat(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<Message>,
    onNavigateToSettings: () -> Unit
) {
    AppLayout(
        topBarContentRight = {
            VerticalCenter {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }
        }
    ) {
        Chat(messages)
    }
}

@Composable
fun ProfileSettings() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Profile",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Image(
                    painter = painterResource(R.drawable.profile_picture),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Nickname",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Kamppi",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 22.sp
                        )
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onNavigateToChat: () -> Unit
) {
    AppLayout(
        topBarContentLeft = {
            VerticalCenter {
                IconButton(onClick = onNavigateToChat) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                }
            }
        }
    ) {
        ProfileSettings()
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
