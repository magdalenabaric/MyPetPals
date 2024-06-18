package com.example.mypetpals.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.mypetpals.viewmodel.AuthViewModel
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypetpals.R

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter){
        Image(
            painter = painterResource(id = R.drawable.slika2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),

            )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to",
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 60.dp)
            )
        }
        Column(

            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White, shape = RoundedCornerShape(
                        topStart = 25.dp,
                        topEnd = 25.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .padding(vertical = 60.dp)
                .height(450.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                modifier = Modifier.border(3.dp, Brush.linearGradient(listOf(Color(0xFFffb281), Color(0xFFff6969)) ), RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.Black,
                    unfocusedBorderColor = Color.White,

                    focusedBorderColor = Color.White,
                    unfocusedLabelColor = Color.Black,
                ),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },

            )
            Spacer(modifier = Modifier.height(30.dp))
            OutlinedTextField(
                modifier = Modifier.border(3.dp, Brush.linearGradient(listOf(Color(0xFFffb281), Color(0xFFff6969)) ), RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.Black,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,

                    unfocusedLabelColor = Color.Black,
                ),

                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(50.dp))


            Button(onClick = {
                authViewModel.signIn(email, password) { success ->
                    if (success) {
                        Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate("profile")
                    } else {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                    }

                }

            }
                ,
                colors = ButtonDefaults.buttonColors(Color(0xFFff6969), contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .width(260.dp)
                    .padding(horizontal = 32.dp)
                    .height(60.dp),

                shape = RoundedCornerShape(16.dp))
            {
                Text(text = "Login", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(90.dp))


            Row {


            Text(text = "You don't have an account?")
                Spacer(modifier = Modifier.width(10.dp))

                ClickableText(
                    text = AnnotatedString("Register"),
                    style = TextStyle(
                        color = Color(0xFFff6969), // Prilagodite boju teksta prema potrebi
                        textDecoration = TextDecoration.Underline, // Dodajte podcrtavanje ako je potrebno
                        fontSize = 18.sp // Prilagodite veličinu teksta prema potrebi,
                    ),
                    onClick = {
                        navController.navigate("Register")

                    })

            }
        }

    }

}