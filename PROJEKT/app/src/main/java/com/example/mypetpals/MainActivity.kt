package com.example.mypetpals

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mypetpals.ui.theme.MyPetPalsTheme
import com.example.mypetpals.view.InitialScreen
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mypetpals.ui.theme.MyPetPalsTheme
import com.example.mypetpals.view.LoginScreen
import com.example.mypetpals.view.NotificationScreen
import com.example.mypetpals.view.PetProfileScreen
import com.example.mypetpals.view.PetsScreen
import com.example.mypetpals.view.ProfileScreen
import com.example.mypetpals.view.RegisterScreen
import com.example.mypetpals.viewmodel.AuthViewModel
import com.example.mypetpals.viewmodel.NotificationViewModel
import com.example.mypetpals.viewmodel.PetsViewModel


class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPetPalsTheme {

                    MyApp()

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
            }
        }
    }


}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val petsViewModel: PetsViewModel = viewModel()
// Provjera je li korisnik prijavljen
    val startDestination = if (authViewModel.currentUser != null) "profile" else "initial"
    NavHost(navController = navController, startDestination = startDestination) {
        composable("initial") {
            InitialScreen(navController = navController)
        }
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController, authViewModel) }
        composable("profile") { ProfileScreen(navController, authViewModel, petsViewModel) }
        composable("pets_screen") { PetsScreen(navController, authViewModel) }
        composable("petProfile/{petId}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            PetProfileScreen(petId = petId, navController, petsViewModel, authViewModel)
        }
        composable("notification_screen") {
            val notificationsViewModel: NotificationViewModel = viewModel()
            NotificationScreen(navController, notificationsViewModel)
        }
    }
}