    package com.example.mypetpals.view

    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.net.Uri
    import android.os.Environment
    import android.widget.Toast
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.horizontalScroll
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
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
    import androidx.compose.foundation.layout.windowInsetsTopHeight
    import androidx.compose.foundation.layout.wrapContentHeight
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material3.Button
    import androidx.compose.material3.Icon
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.unit.dp
    import androidx.core.content.FileProvider
    import androidx.navigation.NavController
    import coil.compose.rememberAsyncImagePainter
    import com.example.mypetpals.R
    import com.example.mypetpals.viewmodel.AuthViewModel
    import java.io.File
    import java.text.SimpleDateFormat
    import java.util.Date
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.CameraAlt
    import androidx.compose.material.icons.filled.Edit
    import androidx.compose.material3.Surface
    import androidx.compose.ui.unit.sp
    import java.util.Locale

    import androidx.compose.material.BottomNavigation
    import androidx.compose.material.BottomNavigationItem
    import androidx.compose.material.icons.filled.AccountCircle
    import androidx.compose.material.icons.filled.Notifications
    import androidx.compose.material.icons.filled.Pets
    import androidx.compose.material.icons.filled.Logout
    import androidx.compose.material.icons.filled.PhotoLibrary
    import androidx.compose.material3.AlertDialog
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.TextField
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.livedata.observeAsState
    import com.example.mypetpals.ui.theme.MPlusRounded
    import com.example.mypetpals.viewmodel.PetsViewModel

    @Composable
    fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel, petsViewModel: PetsViewModel) {
        val user = authViewModel.currentUser
        var name by remember { mutableStateOf(user?.displayName ?: "") }
        var email by remember { mutableStateOf(user?.email ?: "") }
        var imageUrl by remember { mutableStateOf(user?.photoUrl?.toString().orEmpty()) }
        val context = LocalContext.current
        var photoUri by remember { mutableStateOf<Uri?>(null) }
        var isEditing by remember { mutableStateOf(false) }

        val pets by petsViewModel.pets.observeAsState(emptyList())

        val resultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                val uri = data ?: photoUri

                if (uri != null) {
                    authViewModel.uploadProfileImage(uri) { url ->
                        if (url.isNotEmpty()) {
                            imageUrl = url
                            Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show()
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
                    authViewModel.uploadProfileImage(uri) { url ->
                        if (url.isNotEmpty()) {
                            imageUrl = url
                            Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            petsViewModel.getUserPets2()
        }

        if (isEditing) {
            AlertDialog(
                onDismissRequest = { isEditing = false },
                confirmButton = {
                    Button(onClick = {
                        authViewModel.updateProfile(name, email) { success ->
                            if (success) {
                                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                            isEditing = false
                        }
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
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") }
                        )
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") }
                        )
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.slika3),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
,
                        horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()

                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
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
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ){
                            Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
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
                                color = Color(0xFFff716c),
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
                                color = Color(0xFFff716c),
                                contentColor = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = "Take Profile Picture")
                                }
                            }
                        }}
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ){Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column {
                                Text(name, fontSize = 40.sp, color = Color.White)
                                Text(email, color = Color.White)
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
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                                }
                            }
                        }
                    }}

                    item {

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                .background(
                                    Color.White, shape = RoundedCornerShape(
                            topStart = 25.dp,
                            topEnd = 25.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                            )
                            ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(40.dp))

                                Text(text = "My pets", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(40.dp))

                                if (pets.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp)
                                            .background(
                                                color = Color(0xFFdfdfdf),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                    ) {
                                        pets.forEach { pet ->
                                            Column(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                if (pet.imageUrl.isNotEmpty()) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(pet.imageUrl),
                                                        contentDescription = pet.name,
                                                        modifier = Modifier
                                                            .size(130.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Gray),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(100.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Gray),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = "No Image", color = Color.White)
                                                    }
                                                }
                                                Text(text = pet.name, color = Color.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(60.dp))

                                    Button(onClick = { navController.navigate("pets_screen") },
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 6.dp
                                        ),
                                        colors = ButtonDefaults.buttonColors(Color(0xFFff716c), contentColor = Color.White),
                                    ) {

                                        Text(text = "Show pets", color = Color.White, fontFamily = MPlusRounded)
                                    }

                                } else {
                                    Text(text = "No pets available", color = Color.Gray)

                                }
                                Spacer(modifier = Modifier.height(200.dp))



                        }

                    }

                }
            }

            BottomNavigation(
                modifier = Modifier.align(Alignment.BottomCenter),
                backgroundColor = Color(0xFFdfdfdf),
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    selectedContentColor = Color.Gray,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Pets, contentDescription = "Pets") },
                    selected = false,
                    onClick = { navController.navigate("pets_screen") },
                    selectedContentColor = Color.Gray,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") },
                    selected = false,
                    onClick = { navController.navigate("notification_screen") },
                    selectedContentColor = Color.Gray,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Logout, contentDescription = "Logout") },
                    selected = false,
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login")
                    },
                    selectedContentColor = Color.Gray,
                    unselectedContentColor = Color.Gray
                )
            }
        }
    }

    private fun createImageFile(context: Context): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }