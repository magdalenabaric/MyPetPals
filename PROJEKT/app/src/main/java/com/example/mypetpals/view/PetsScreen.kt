package com.example.mypetpals.view

import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.mypetpals.R
import com.example.mypetpals.model.Pet
import com.example.mypetpals.viewmodel.PetsViewModel
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mypetpals.ui.theme.MPlusRounded
import com.example.mypetpals.viewmodel.AuthViewModel

@Composable
fun PetsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val petsViewModel: PetsViewModel = viewModel()
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newPetName by remember { mutableStateOf("") }
    var newPetAnimal by remember { mutableStateOf("") }
    var newPetAge by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var speciesError by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    LaunchedEffect(Unit) {
        petsViewModel.getUserPets { userPets ->
            pets = userPets
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Add New Pet") },
            text = {
                Column {
                    TextField(
                        value = newPetName,
                        onValueChange = { newPetName = it },
                        label = { Text(text = "Pet Name") },
                        isError = nameError != null
                    )
                    if (nameError != null) {
                        Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newPetAnimal,
                        onValueChange = { newPetAnimal = it },
                        label = { Text(text = "Pet Species") },
                        isError = speciesError != null
                    )
                    if (speciesError != null) {
                        Text(text = speciesError!!, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newPetAge,
                        onValueChange = { newPetAge = it },
                        label = { Text(text = "Pet Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { launcher.launch("image/*") }) {
                        Text(text = "Choose Image")
                    }
                    imageUri?.let {
                        Image(painter = rememberAsyncImagePainter(it), contentDescription = null, modifier = Modifier.size(100.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    nameError = if (newPetName.isBlank()) "Pet name is required" else null
                    speciesError = if (newPetAnimal.isBlank()) "Pet species is required" else null

                    if (nameError == null && speciesError == null) {
                        val petAge = newPetAge.toIntOrNull() ?: 0
                        val newPet = Pet(name = newPetName, animal = newPetAnimal, age = petAge)
                        petsViewModel.addPet(newPet, imageUri) { success ->
                            if (success) {
                                petsViewModel.getUserPets { userPets ->
                                    pets = userPets
                                    showDialog = false
                                }
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
            LazyColumn(modifier = Modifier.fillMaxSize()
                .padding(bottom = 56.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Text(text = "My Pets", fontSize = 40.sp, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
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
                    }
                }

                items(pets) { pet ->
                    PetItem(pet, onClick = { navController.navigate("petProfile/${pet.id}") })
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
                selected = true, // Set selected to true for pets screen
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
}

@Composable
fun PetItem(pet: Pet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(170.dp)
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFdfdfdf),
            contentColor = Color.Black
        ),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(pet.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(130.dp)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = pet.name, style = MaterialTheme.typography.titleLarge, fontSize = 36.sp)
                Text(text = pet.animal, style = MaterialTheme.typography.bodyLarge)

            }
        }
    }
}
