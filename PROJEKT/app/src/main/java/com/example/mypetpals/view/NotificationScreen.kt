package com.example.mypetpals.view

import android.app.AlarmManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mypetpals.R
import com.example.mypetpals.model.Notification
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypetpals.viewmodel.NotificationViewModel
import com.google.firebase.Timestamp

import java.util.Calendar
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.example.mypetpals.NotificationReceiver
import com.example.mypetpals.model.Pet
import com.example.mypetpals.ui.theme.MPlusRounded
import com.example.mypetpals.viewmodel.AuthViewModel
import com.example.mypetpals.viewmodel.PetsViewModel
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun NotificationScreen(navController: NavController, notificationsViewModel: NotificationViewModel) {
    val petsViewModel: PetsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var petList by remember { mutableStateOf(listOf<Pet>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newDescription by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf<Timestamp?>(null) }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var showCurrentNotifications by remember { mutableStateOf(true) }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }

    var petError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var timeError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            notificationsViewModel.getUserNotifications { userNotifications ->
                notifications = userNotifications
            }
            petsViewModel.getUserPets { userPets ->
                petList = userPets
            }
        } catch (e: Exception) {
            Log.e("NotificationScreen", "Error fetching data", e)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Add New Notification") },
            text = {
                Column {
                    if (petList.isNotEmpty()) {
                        Box {
                            Button(onClick = { expanded = true }) {
                                Text(selectedPet?.name ?: "Select a pet")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                petList.forEach { pet ->
                                    DropdownMenuItem(onClick = {
                                        selectedPet = pet
                                        expanded = false
                                        petError = ""
                                    }) {
                                        Text(pet.name)
                                    }
                                }
                            }
                        }
                        if (petError.isNotEmpty()) {
                            Text(text = petError)
                        }
                    } else {
                        Text("No pets available")
                    }
                    TextField(
                        value = newDescription,
                        onValueChange = {
                            newDescription = it
                            descriptionError = ""
                        },
                        label = { Text(text = "Description") }
                    )
                    if (descriptionError.isNotEmpty()) {
                        Text(text = descriptionError)
                    }
                    Button(onClick = { showDatePickerDialog(context) { selectedTime ->
                        newTime = selectedTime
                        timeError = ""
                    } }) {
                        Text("Pick Date and Time")
                    }
                    Text(
                        text = "Selected time: ${newTime?.toDate() ?: "None"}",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Gray
                    )
                    if (timeError.isNotEmpty()) {
                        Text(text = timeError)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    var hasError = false
                    if (selectedPet == null) {
                        petError = "Please select a pet"
                        hasError = true
                    }
                    if (newDescription.isEmpty()) {
                        descriptionError = "Please enter a description"
                        hasError = true
                    }
                    if (newTime == null) {
                        timeError = "Please select a date and time"
                        hasError = true
                    }
                    if (hasError) {
                        return@Button
                    }

                    val userId = authViewModel.currentUser?.uid ?: return@Button
                    val newNotification = Notification(
                        id = (notifications.size + 1).toString(),
                        petName = selectedPet?.name ?: "",
                        description = newDescription,
                        time = newTime!!,
                        userId = userId,
                        petId = selectedPet!!.id
                    )
                    notificationsViewModel.addNotification(newNotification, selectedPet!!.id) { success ->
                        if (success) {
                            notificationsViewModel.getUserNotifications { userNotifications ->
                                notifications = userNotifications
                                scheduleNotification(context, newNotification)
                                showDialog = false
                            }
                        }
                    }
                }) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showEditDialog && selectedNotification != null) {
        EditNotificationDialog(
            notification = selectedNotification!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedNotification ->
                notificationsViewModel.updateNotification(updatedNotification) { success ->
                    if (success) {
                        notificationsViewModel.getUserNotifications { userNotifications ->
                            notifications = userNotifications
                        }
                        showEditDialog = false
                    }
                }
            },
            onDelete = { notificationToDelete ->
                notificationsViewModel.deleteNot(notificationToDelete) { success ->
                    if (success) {
                        notificationsViewModel.getUserNotifications { userNotifications ->
                            notifications = userNotifications
                        }
                        cancelNotification(context, notificationToDelete.id.toInt())
                        showEditDialog = false
                    }
                }
            }
        )
    }

    val currentTime = Timestamp.now()
    val currentNotifications = notifications.filter { it.time!! > currentTime }
    val pastNotifications = notifications.filter { it.time!! <= currentTime }

    Box {
        Image(
            painter = painterResource(id = R.drawable.slika3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "My Notifications", fontSize = 40.sp, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(16.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Button(
                            onClick = { showDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF727272),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp
                            ),
                        ) {
                            Text(
                                text = "Add new",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontFamily = MPlusRounded
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showCurrentNotifications = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = if (showCurrentNotifications) Color.White else Color(0xFFDBDBDB)
                                ),
                            ) {
                                Text(text = "Current", fontSize = 20.sp, fontFamily = MPlusRounded)
                            }
                            Button(
                                onClick = { showCurrentNotifications = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = if (!showCurrentNotifications) Color.White else Color(0xFFDBDBDB)
                                ),
                            ) {
                                Text(text = "Past", fontSize = 20.sp, fontFamily = MPlusRounded)
                            }
                        }
                    }
                }

                val notificationsToDisplay = if (showCurrentNotifications) currentNotifications else pastNotifications
                items(notificationsToDisplay) { notification ->
                    NotificationItem(
                        notification,
                        petList,
                        onEditClick = {
                            selectedNotification = notification
                            showEditDialog = true
                        }
                    )
                }
            }
        }

        BottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            backgroundColor = Color(0xFFdfdfdf),
            contentColor = Color(0xFF727272)
        ) {
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                selected = false,
                onClick = { navController.navigate("profile") },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Gray
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Pets, contentDescription = "Pets") },
                selected = false,
                onClick = { navController.navigate("pets_screen") },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Gray
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") },
                selected = true, // Set selected to true for notifications
                onClick = { navController.navigate("notification_screen") },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Gray
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Logout, contentDescription = "Logout") },
                selected = false,
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login")
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Gray
            )
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, petList: List<Pet>, onEditClick: () -> Unit) {
    val context = LocalContext.current
    val pet = petList.find { it.name == notification.petName }
    val petImageUrl = pet?.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEditClick() },
        elevation = 4.dp,
        backgroundColor = Color(0xFFdfdfdf),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (petImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(petImageUrl),
                    contentDescription = pet?.name,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(70.dp)
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Pet: ${notification.petName}", fontSize = 20.sp, color = Color.Black)
                Text(text = "Description: ${notification.description}", fontSize = 16.sp, color = Color.Black)
                Text(text = "Time: ${notification.time?.toDate()}", fontSize = 16.sp, color = Color.Black)
            }
        }
    }
}

@Composable

fun EditNotificationDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    onSave: (Notification) -> Unit,
    onDelete: (Notification) -> Unit
) {
    var description by remember { mutableStateOf(notification.description) }
    var newTime by remember { mutableStateOf<Timestamp?>(notification.time) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Notification") },
        text = {
            Column {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description") }
                )

            }
        },
        confirmButton = {
            Button(onClick = {
                if (newTime == null) {
                    return@Button
                }
                val updatedNotification = notification.copy(
                    description = description,
                    time = newTime!!
                )
                onSave(updatedNotification)
                onDismiss()
            }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            Column {
                Button(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    onDelete(notification)
                    onDismiss()
                }) {
                    Text(text = "Delete")
                }
            }
        }
    )
}

fun cancelNotification(context: Context, notificationId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}

fun scheduleNotification(context: Context, notification: Notification) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("petName", notification.petName)
            putExtra("description", notification.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = notification.time?.toDate()?.time ?: System.currentTimeMillis()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    } catch (e: Exception) {
        Log.e("NotificationScreen", "Error scheduling notification", e)
    }
}

fun showDatePickerDialog(context: Context, onDateSelected: (Timestamp) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                    onDateSelected(Timestamp(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}





