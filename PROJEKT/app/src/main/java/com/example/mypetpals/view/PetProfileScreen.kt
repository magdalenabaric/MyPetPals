package com.example.mypetpals.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mypetpals.R
import com.example.mypetpals.model.Notification
import com.example.mypetpals.model.Pet
import com.example.mypetpals.ui.theme.MPlusRounded
import com.example.mypetpals.viewmodel.AuthViewModel
import com.example.mypetpals.viewmodel.NotificationViewModel
import com.example.mypetpals.viewmodel.PetsViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun PetProfileScreen(
    petId: String?,
    navController: NavController,
    petsViewModel: PetsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    notificationsViewModel: NotificationViewModel = viewModel()
) {
    var pet by remember { mutableStateOf<Pet?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(listOf<Notification>()) }

    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var petImageUrl by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    var petName by remember { mutableStateOf(pet?.name ?: "") }
    var petAge by remember { mutableStateOf(pet?.age ?: 0) }
    var petSpec by remember { mutableStateOf("") }

    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            val uri = data ?: photoUri

            if (uri != null) {
                if (petId != null) {
                    petsViewModel.uploadPetImage(petId, uri) { url ->
                        if (url.isNotEmpty()) {
                            petImageUrl = url
                            Toast.makeText(context, "Pet image updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update pet image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                if (petId != null) {
                    petsViewModel.uploadPetImage(petId, uri) { url ->
                        if (url.isNotEmpty()) {
                            petImageUrl = url
                            Toast.makeText(context, "Pet image updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update pet image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(petId) {
        petId?.let {
            Log.d("PetProfileScreen", "Fetching pet with id: $petId")
            petsViewModel.getPetById(it) { fetchedPet ->
                Log.d("PetProfileScreen", "Fetched pet: $fetchedPet")
                pet = fetchedPet
                petName = fetchedPet?.name ?: ""
                petAge = fetchedPet?.age ?: 0
                petImageUrl = fetchedPet?.imageUrl ?: ""
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    LaunchedEffect(pet) {
        pet?.let { currentPet ->
            notificationsViewModel.getUserNotifications { userNotifications ->
                notifications = userNotifications.filter { it.petName == currentPet.name }
                Log.d("PetProfileScreen", "Filtered notifications: $notifications")
            }
        }
    }

    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            confirmButton = {
                Button(onClick = {
                    petId?.let { id ->
                        petsViewModel.updatePet(id, petName, petSpec, petAge) { success ->
                            if (success) {
                                Toast.makeText(context, "Pet updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to update pet", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    isEditing = false
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                Button(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            },
            text = {
                Column {
                    TextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Name") }
                    )
                    TextField(
                        value = petAge.toString(),
                        onValueChange = { petAge = it.toIntOrNull() ?: 0 },
                        label = { Text("Age") }
                    )
                }
            }
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {



    if (isLoading) {
        Text(text = "Loading...", modifier = Modifier.padding(16.dp))
    } else {
        pet?.let {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Image(
                    painter = painterResource(id = R.drawable.slika3),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .fillMaxWidth(),
                    ){
            item{
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ){
                    if (petImageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(petImageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No Image", color = Color.White)
                        }
                    }
}}
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ){
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_PICK).apply {
                                        type = "image/*"
                                    }
                                    resultLauncher.launch(intent)
                                },
                            color = Color(0xFFFF716C),
                            contentColor = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Change Profile Picture")
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val photoFile = createImageFile(context)
                                    photoFile?.also {
                                        photoUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            it
                                        )
                                        photoUri?.let { uri ->
                                            takePictureLauncher.launch(uri)
                                        }
                                    }
                                },
                            color = Color(0xFFFF716C),
                            contentColor = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Take Pet Picture")
                            }
                        }
                    }
                            }}
                        item{
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ){
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End) {


                    Column {
                        Text(it.name, fontSize = 40.sp, color = Color.White)

                        Text("Species: ${it.animal}", fontSize = 20.sp, color = Color.White)
                        Text("Age: ${it.age}", fontSize = 20.sp, color = Color.White)
                    }
                        Spacer(modifier = Modifier.width(30.dp))

                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isEditing = true
                                },
                            color = Color(0xFFff716c),
                            contentColor = Color.White,

                            ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Pet")
                            }
                        }
                    }}}


item{

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ){
                    Button(
                        onClick = {
                            petId?.let { id ->
                                petsViewModel.deletePet(id) {
                                    Toast.makeText(context, "Pet deleted", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        },
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp
                        ),
                        colors = ButtonDefaults.buttonColors(Color(0xFF727272), contentColor = Color.White)
                    ) {
                        Text("Delete Pet", color = Color.White, fontFamily = MPlusRounded)
                    }
}}

                    item{
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White, shape = RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp
                            )
                        )
                        .padding(vertical = 50.dp)
                        .height(300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Notifications", fontSize = 36.sp)
                    LazyColumn {
                        items(notifications) { notification ->
                            NotificationItem2(notification)
                        }
                    }
                }
                }}}}
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
                        selected = false,
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
        } ?: run {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Pet not found")
                Text(text = "Pet ID: $petId", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun NotificationItem2(notification: Notification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFdfdfdf),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Pet: ${notification.petName}", fontSize = 20.sp, color = Color.Black)
            Text(text = "Description: ${notification.description}", fontSize = 16.sp, color = Color.Black)
            Text(text = "Time: ${notification.time?.toDate()}", fontSize = 16.sp, color = Color.Black)
        }
    }
}

private fun createImageFile(context: Context): File? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
