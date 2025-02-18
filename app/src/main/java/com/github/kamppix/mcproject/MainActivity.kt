package com.github.kamppix.mcproject

import SampleData
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.kamppix.mcproject.ui.theme.MCProjectTheme
import kotlinx.serialization.Serializable
import java.io.File


class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var prevLightValue = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        createNotificationChannel()

        setContent {
            MCProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) prevLightValue = lightSensor!!.maximumRange
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(NotificationChannel(
            "test",
            "Test notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }

    private fun sendTestNotification() {
        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        // Initialize intent
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(this, "test")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Test notification")
            .setContentText("This is a test notification! You are reading this test notification because it was sent to you.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Send notification
        NotificationManagerCompat.from(this).notify(0, builder.build())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]

            if (lux != prevLightValue) {
                if (lux == event.sensor.maximumRange) {
                    // Check permission
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

                    // Initialize intent
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0,
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        },
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    // Build notification
                    val builder = NotificationCompat.Builder(this, "test")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Light sensor maxed")
                        .setContentText("The light sensor has reached its max value!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    // Send notification
                    NotificationManagerCompat.from(this).notify(1, builder.build())
                }

                // Save value
                prevLightValue = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Ignore
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
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
                        sendTestNotification()
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
