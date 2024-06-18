package com.example.mypetpals.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypetpals.R
import com.example.mypetpals.ui.theme.MyPetPalsTheme
import androidx.navigation.NavController


@Composable
fun InitialScreen(navController: NavController?) {
    Box (){ Image(
        painter = painterResource(id = R.drawable.slika1),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),

    )
        Column {
            Spacer(modifier = Modifier.height(585.dp))

            Button(onClick = {
                navController?.navigate("login")
            },
                colors = ButtonDefaults.buttonColors(Color(0xFF727272), contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(70.dp),

                shape = RoundedCornerShape(25.dp))
            {
                Text(text = "LOGIN",
                    fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.height(30.dp)) // Dodajte razmak odozgo

            Button(onClick = {
                navController?.navigate("register")
            },
                colors = ButtonDefaults.buttonColors(Color(0xFF727272), contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                ),

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(70.dp),
                shape = RoundedCornerShape(25.dp))
            {
                    Text(text = "SIGNUP",
                        fontSize = 26.sp)

            }
        }
    }
}
