package com.example.brewspot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.brewspot.navigation.AppNavigation
import com.example.brewspot.ui.theme.MyAppTheme
import com.example.brewspot.view.login.LoginViewModel
import com.example.brewspot.ui.theme.Purple40
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                AppNavigation()
            }
        }
    }
}